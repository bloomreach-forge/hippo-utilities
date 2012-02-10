package org.onehippo.forge.utilities.repository.scheduler;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import java.util.PropertyResourceBundle;

import javax.jcr.LoginException;
import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.observation.Event;
import javax.jcr.observation.EventIterator;
import javax.jcr.observation.EventListener;
import javax.jcr.query.Query;
import javax.jcr.query.QueryResult;

import org.apache.commons.lang.StringUtils;
import org.hippoecm.repository.HippoRepository;
import org.hippoecm.repository.HippoRepositoryFactory;
import org.hippoecm.repository.ext.DaemonModule;
import org.quartz.CronTrigger;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.quartz.TriggerUtils;
import org.quartz.impl.StdSchedulerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The schuler daemon.
 */
public class SchedulerDaemon implements DaemonModule, EventListener {

  private static final Logger logger = LoggerFactory.getLogger(SchedulerDaemon.class);

  private static final String SCHEDULER_PATH_QUERY = "//element(*, scheduler:jobScheduleGroup)";

  private final DateFormat dateFormat = new SimpleDateFormat("yyyyMMdd-HHmmss");

  private Scheduler scheduler;
  private PropertyResourceBundle projectProperties;

  private static final String PROJECT_CONFIG = "project.config";
  private static final String PROJECT_PROPERTY_SCHEDULER_ACTIVE = "scheduler.active";
  private static final String PROJECT_PROPERTY_REPOSITORY_ADDRESS = "scheduler.repository.address";
  private static final String PROJECT_PROPERTY_REPOSITORY_USER = "scheduler.repository.user";
  private static final String PROJECT_PROPERTY_REPOSITORY_PASSWORD = "scheduler.repository.password";

  protected final List<JobScheduleGroup> jobScheduleGroups = new ArrayList<JobScheduleGroup>();
  private Session session;
  private Node schedulerNode;


  /**
   * Initializes the daemon, loads the configuration, starts the scheduler and creates the jobs.
   *
   * @param session the jcr session.
   * @throws javax.jcr.RepositoryException if registering the event listener on the configuration node in the repository fails.
   */
  public void initialize(final Session session) throws RepositoryException {
    try {
      projectProperties = getProjectProperties();
    } catch (IOException e) {
      logger.error("Error in loading project.properties");
    }
    final String schedulerActive = projectProperties.getString(PROJECT_PROPERTY_SCHEDULER_ACTIVE);
    if (!Boolean.parseBoolean(schedulerActive)) {
      logger.info("Scheduler daemon will not run on this machine.");
      return;
    }
    this.session = session;
    try {

      // select one scheduler:scheduler node by query
      final Query query = session.getWorkspace().getQueryManager().createQuery(SCHEDULER_PATH_QUERY, Query.XPATH);
      final QueryResult result = query.execute();
      if (!result.getNodes().hasNext()) {
        logger.info("Scheduler Daemon not configured, can't find node by type {}", Namespace.NodeType.SCHEDULER);
        return;
      }

      schedulerNode = result.getNodes().nextNode();

      // FIXME TODO check property 'scheduler:active' on schedulerNode; do not create JCRScheduler if false
      // (also in onEvent!)

      // FIXME TODO do not create/start JCRScheduler if there are no active schedules (als in onEvent)

      final Properties properties = setup(schedulerNode);
      StdSchedulerFactory factory = new StdSchedulerFactory();
      factory.initialize(properties);
      scheduler = factory.getScheduler();
      scheduler.start();

      loadScheduleGroups();
      killAllJobs();
      scheduleJobs(projectProperties);

      // register listener so we can re-configure the jobs:
      session.getWorkspace().getObservationManager().addEventListener(this,
              Event.NODE_ADDED | Event.PROPERTY_CHANGED, schedulerNode.getPath(), true, null, null, true);

    } catch (SchedulerException se) {
      logger.error("Error initializing scheduler.", se);
    }

  }

  /**
   * Is called when the module is shutting down. Kills all jobs and logs out the session.
   */
  public void shutdown() {
    logger.info("+------------------------------------------+");
    logger.info("|         Shutting  down jobs              |");
    logger.info("+------------------------------------------+");
    if (scheduler != null) {
      killAllJobs();
      try {
        scheduler.shutdown();
      } catch (SchedulerException e) {
        e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
      }
    }
    session.logout();
  }

  /**
   * If the configuration node changes, this method is triggered. It reloads the configuration, stops the jobs and
   * creates the new.
   *
   * @param events the events
   */
  public void onEvent(EventIterator events) {
    logger.info(" Reloading Scheduler configuration ");
    if (logger.isDebugEnabled()) {
      logger.debug("Reloading scheduler configuration.");
    }
    try {
      loadScheduleGroups();
      killAllJobs();
      if (projectProperties != null) {
        scheduleJobs(projectProperties);
      }
    } catch (RepositoryException e) {
      logger.error("RepositoryException on events {}", events, e);
    }
  }

  /**
   * Setup the configuration for the quartz scheduler.
   *
   * @return the configuration properties for the quartz scheduler
   */
  private Properties setup(Node node) throws RepositoryException {
    Properties properties = new Properties();
    properties.put("org.quartz.scheduler.instanceName",
            NodeUtils.getString(node, "org.quartz.scheduler.instanceName", "Job Scheduler"));
    properties.put("org.quartz.scheduler.instanceId",
            NodeUtils.getString(node, "org.quartz.scheduler.instanceId", "AUTO"));
    properties.put("org.quartz.threadPool.class",
            NodeUtils.getString(node, "org.quartz.threadPool.class", "org.quartz.simpl.SimpleThreadPool"));
    properties.put("org.quartz.threadPool.threadCount",
            NodeUtils.getString(node, "org.quartz.threadPool.threadCount", "1"));
    properties.put("org.quartz.threadPool.threadPriority",
            NodeUtils.getString(node, "org.quartz.threadPool.threadPriority", "5"));
    properties.put("org.quartz.jobStore.misfireThreshold",
            NodeUtils.getString(node, "org.quartz.jobStore.misfireThreshold", "60000"));
    properties.put("org.quartz.jobStore.class",
            NodeUtils.getString(node, "org.quartz.jobStore.class", "org.quartz.simpl.RAMJobStore"));
    return properties;
  }

  protected void loadScheduleGroups() throws RepositoryException {
    jobScheduleGroups.clear();
    // load job schedule groups
    final NodeIterator iterator = schedulerNode.getNodes();
    while (iterator.hasNext()) {
      final Node subNode = iterator.nextNode();
      if (subNode.isNodeType(Namespace.NodeType.JOB_SCHEDULE)) {
        jobScheduleGroups.add(new JobScheduleGroup(schedulerNode));
      }
    }
  }

  /**
   * Kill all existing Quartz jobs
   */
  protected void killAllJobs() {
    // kill all jobs - means kill all existing, also if they are not configured anymore
    if (scheduler == null) {
      return;
    }

    try {
      for (String groupName : scheduler.getJobGroupNames()) {
        logger.info("Killing all quartz jobs in group: " + groupName + " ***");
        final String[] jobNames = scheduler.getJobNames(groupName);
        for (String jobName : jobNames) {
          if (logger.isInfoEnabled()) {
            logger.info("  Killing quartz job {}", jobName);
          }

          // try per job
          try {
            scheduler.deleteJob(jobName, groupName);
          } catch (SchedulerException se) {
            logger.error("Error occurred deleting job " + jobName + " from group " + groupName, se);
          }
        }
      }
    } catch (SchedulerException e) {
      logger.error("Error occurred getting job group or job names ", e);
    }
  }

  /**
   * Schedules the configured jobs
   */
  protected void scheduleJobs(PropertyResourceBundle projectProperties) {
    try {
      for (JobScheduleGroup group : jobScheduleGroups) {
        if (group.active()) {
          for (JobSchedule jobSchedule : group.getJobSchedules()) {
            if (jobSchedule.active()) {
              scheduleJob(jobSchedule, projectProperties);
            }
          }
        }
      }
    } catch (SchedulerException exception) {
      logger.error("Error scheduling jobs", exception);
    } catch (UnsupportedOperationException e) {
      // thrown by Quartz when the user specified both a day-of-week AND a day-of-month parameter
      logger.error("Error scheduling jobs", e);
    }
  }

  /**
   * Schedules a job
   *
   * @param jobSchedule the configuration for the revision mail job
   * @throws org.quartz.SchedulerException if scheduling the revision mail job fails
   */
  protected void scheduleJob(JobSchedule jobSchedule, PropertyResourceBundle projectProperties) throws SchedulerException {
    try {
      // load job class
      Class jobClass = Class.forName(jobSchedule.getJobClassName());

      // prepare data for usage in jobs
      final JobDataMap dataMap = new JobDataMap();
      dataMap.put(Session.class.getSimpleName(), session);
      dataMap.put(JobConfiguration.class.getSimpleName(), jobSchedule.getJobConfiguration());
      dataMap.put(PROJECT_PROPERTY_REPOSITORY_ADDRESS, projectProperties.getString("repository.url"));
      dataMap.put(PROJECT_PROPERTY_REPOSITORY_USER, projectProperties.getString("repository.user"));
      dataMap.put(PROJECT_PROPERTY_REPOSITORY_PASSWORD, projectProperties.getString("repository.password"));
      // immediate or cron
      // be aware that if runInstantly is on and and is active, with every change of the
      // configuration node the job will be // triggered if the job is set to runNow .
      final String jobName = jobSchedule.getJobName() + getDateString();
      Trigger trigger;
      if (jobSchedule.runInstantly()) {
        trigger = TriggerUtils.makeImmediateTrigger(0, 0);
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date());
        calendar.add(Calendar.SECOND, 45);
        trigger.setStartTime(calendar.getTime());
      } else {
        trigger = new CronTrigger();
        ((CronTrigger) trigger).setCronExpression(jobSchedule.getCronExpression());
      }

      trigger.setJobDataMap(dataMap);
      trigger.setName(jobName);
      trigger.setGroup(jobSchedule.getGroupName());

      scheduler.scheduleJob(new JobDetail(jobName, jobSchedule.getGroupName(), jobClass), trigger);
      logger.info("scheduled job {}", jobSchedule);
    } catch (ParseException exception) {
      logger.error("Cron parse error on cron expression " + jobSchedule.getCronExpression() +
              " scheduling quartz job " + jobSchedule.getJobName(), exception);
    } catch (ClassNotFoundException cce) {
      logger.error("ClassNotFoundException for class " + jobSchedule.getJobClassName() +
              " scheduling quartz job " + jobSchedule.getJobName(), cce);
    }
  }

  protected String getDateString() {
    Calendar now = Calendar.getInstance();
    return dateFormat.format(now.getTime());
  }


  public static Session getSession(JobDataMap dataMap) throws LoginException, RepositoryException {
    HippoRepository repository = HippoRepositoryFactory.getHippoRepository((String) dataMap.get(SchedulerDaemon.PROJECT_PROPERTY_REPOSITORY_ADDRESS));
    Session session = repository.login((String) dataMap.getString(SchedulerDaemon.PROJECT_PROPERTY_REPOSITORY_USER), ((String) dataMap.get(SchedulerDaemon.PROJECT_PROPERTY_REPOSITORY_PASSWORD)).toCharArray());
    return session;
  }

  private PropertyResourceBundle getProjectProperties() throws IOException {
    InputStream propertiesStream;
    final String projectPropertiesPath = System.getProperty(PROJECT_CONFIG);
    if (StringUtils.isNotBlank(projectPropertiesPath)) {
      logger.info("Loading project properties from file '" + projectPropertiesPath + "'");
      propertiesStream = new FileInputStream(projectPropertiesPath);
    } else {
      logger.info("Loading project properties from resource '/project.properties'");
      propertiesStream = SchedulerDaemon.class.getClassLoader().getResourceAsStream("/project.properties");
    }
    return new PropertyResourceBundle(propertiesStream);
  }
}
