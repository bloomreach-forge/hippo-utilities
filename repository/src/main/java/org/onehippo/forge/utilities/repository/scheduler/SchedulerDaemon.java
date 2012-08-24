/*
 * Copyright 2012 Hippo
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.onehippo.forge.utilities.repository.scheduler;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Properties;
import java.util.PropertyResourceBundle;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.observation.Event;
import javax.jcr.observation.EventIterator;
import javax.jcr.observation.EventListener;
import javax.jcr.query.Query;
import javax.jcr.query.QueryResult;

import org.apache.commons.lang.StringUtils;
import org.hippoecm.repository.ext.DaemonModule;
import org.onehippo.forge.utilities.repository.scheduler.jcr.JCRSchedulerFactory;
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

import org.onehippo.forge.utilities.commons.NodeUtils;

/**
 * The scheduler daemon.
 */
public class SchedulerDaemon implements DaemonModule, EventListener {

    private static final String SCHEDULER_PROPERTIES = "scheduler.properties";
    private static final String SCHEDULER_PROPERTY_SCHEDULER_ACTIVE = SchedulerDaemon.class.getName() + ".active";

    private static final Logger logger = LoggerFactory.getLogger(SchedulerDaemon.class);

    private static final String SCHEDULER_QUERY = "//element(*, " + Namespace.NodeType.SCHEDULER + ")";

    private final DateFormat dateFormat = new SimpleDateFormat("yyyyMMdd-HHmmss");

    protected Session session;
    protected SchedulerNode schedulerNode;
    protected Scheduler quartzScheduler;

    /**
     * Initializes the daemon, loads the configuration, starts the scheduler and creates the jobs.
     *
     * @param session the jcr session.                                          2
     * @throws javax.jcr.RepositoryException if registering the event listener on the configuration node in the repository fails.
     */
    public void initialize(final Session session) throws RepositoryException {

        final PropertyResourceBundle projectProperties;
        try {
            projectProperties = getProjectProperties();
        } catch (IOException e) {
            logger.error("Error loading project properties for scheduler, scheduler deactivated.");
            return;
        }

        final String schedulerActive = projectProperties.getString(SCHEDULER_PROPERTY_SCHEDULER_ACTIVE);
        if (!Boolean.parseBoolean(schedulerActive)) {
            logger.info("Scheduler daemon is deactivated.");
            return;
        }

        this.session = session;

        // select one scheduler:scheduler node by query
        final Query query = session.getWorkspace().getQueryManager().createQuery(SCHEDULER_QUERY, Query.XPATH);
        final QueryResult result = query.execute();
        if (!result.getNodes().hasNext()) {
            logger.info("Scheduler Daemon not configured, can't find node by type {}", Namespace.NodeType.SCHEDULER);
            return;
        }

        schedulerNode = createSchedulerNode(result.getNodes().nextNode());

        createQuartzScheduler();
        killAllJobs();
        scheduleJobs();

        // register listener so we can re-configure the jobs:
        session.getWorkspace().getObservationManager().addEventListener(this,
                Event.NODE_ADDED | Event.PROPERTY_CHANGED, schedulerNode.getNode().getPath(), true, null, null, true);
    }

    /**
     * Is called when the module is shutting down. Kills all jobs and logs out the session.
     */
    public void shutdown() {
        logger.info("+-----------------------------------------+");
        logger.info("|         Shutting down jobs              |");
        logger.info("+-----------------------------------------+");
        killAllJobs();
        destroyQuartzScheduler();
        session.logout();
    }

    /**
     * If the configuration node changes, this method is triggered. It reloads the configuration, stops the jobs and
     * creates the new.
     *
     * @param events the events
     */
    public void onEvent(EventIterator events) {
        logger.info("Reloading scheduler configuration");
        try {
            schedulerNode.reload();
            killAllJobs();
            if (!schedulerNode.active()) {
                destroyQuartzScheduler();
            }
            else {
                createQuartzScheduler();
                scheduleJobs();
            }
        } catch (RepositoryException e) {
            logger.error("RepositoryException on events {}", events, e);
        }
    }

    /**
     * Get the configuration for the quartz scheduler, configured in the top scheduler node.
     *
     * @return the configuration properties for the quartz scheduler
     */
    protected Properties getQuartzSchedulerConfiguration(Node node) throws RepositoryException {
        Properties properties = new Properties();
        properties.put("org.quartz.scheduler.instanceName",
                NodeUtils.getString(node, "org.quartz.scheduler.instanceName", "Hippo Utilities Quartz Job Scheduler"));
        properties.put("org.quartz.scheduler.instanceId",
                NodeUtils.getString(node, "org.quartz.scheduler.instanceId", "AUTO"));
        properties.put("org.quartz.scheduler.skipUpdateCheck",
                NodeUtils.getString(node, "org.quartz.scheduler.skipUpdateCheck", "true"));
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

    /**
     * Create an scheduler node object
     * @param node JCR node
     * @return SchedulerNode object
     * @throws RepositoryException
     */
    protected SchedulerNode createSchedulerNode(final Node node) throws RepositoryException {
        return new SchedulerNode(node);
    }

    /**
     * Kill all existing Quartz jobs
     */
    protected void killAllJobs() {

        // kill all jobs - means kill all existing, also if they are not configured anymore
        if (quartzScheduler == null) {
            return;
        }

        try {
            for (String groupName : quartzScheduler.getJobGroupNames()) {
                logger.info("Killing all quartz jobs in group: " + groupName + " ***");
                final String[] jobNames = quartzScheduler.getJobNames(groupName);
                for (String jobName : jobNames) {
                    if (logger.isInfoEnabled()) {
                        logger.info("Killing quartz job {}", jobName);
                    }

                    // try per job
                    try {
                        quartzScheduler.deleteJob(jobName, groupName);
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
    protected void scheduleJobs() {

        if (quartzScheduler == null) {
            return;
        }

        try {
            for (JobScheduleGroup group : schedulerNode.getJobScheduleGroups()) {
                if (group.active()) {
                    for (JobSchedule jobSchedule : group.getJobSchedules()) {
                        if (jobSchedule.active()) {
                            scheduleJob(jobSchedule);
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
    protected void scheduleJob(JobSchedule jobSchedule) throws SchedulerException {
        try {
            // load job class
            Class jobClass = Class.forName(jobSchedule.getJobClassName());

            // prepare data for usage in jobs
            final JobDataMap dataMap = new JobDataMap();
            dataMap.put(JobConfiguration.class.getName(), jobSchedule.getJobConfiguration());

            // immediate or cron
            // be aware that if runInstantly is on and and is active, with every change of the
            // configuration node the job will be triggered if the job is set to runNow
            final String jobName = jobSchedule.getJobName() + getDateString();
            Trigger trigger = null;
            if (jobSchedule.runInstantly()) {
                // runInstantly means after 30 seconds
                trigger = TriggerUtils.makeImmediateTrigger(0, 0);
                Calendar calendar = Calendar.getInstance();
                calendar.setTime(new Date());
                calendar.add(Calendar.SECOND, 30);
                trigger.setStartTime(calendar.getTime());
            } else if (jobSchedule.getCronExpression() != null){
                trigger = new CronTrigger();
                ((CronTrigger) trigger).setCronExpression(jobSchedule.getCronExpression());
            }
            else {
                logger.warn("job {} NOT scheduled: lacking either cron expression or runInstanly flag", jobSchedule);
            }

            if (trigger != null) {
                trigger.setJobDataMap(dataMap);
                trigger.setName(jobName);
                trigger.setGroup(jobSchedule.getGroupName());

                quartzScheduler.scheduleJob(new JobDetail(jobName, jobSchedule.getGroupName(), jobClass), trigger);

                logger.info("scheduled job {}", jobSchedule);
            }
        } catch (ParseException exception) {
            logger.error("Cron parse error on cron expression " + jobSchedule.getCronExpression() +
                    " scheduling quartz job " + jobSchedule.getJobName(), exception);
        } catch (ClassNotFoundException cce) {
            logger.error("ClassNotFoundException for class " + jobSchedule.getJobClassName() +
                    " scheduling quartz job " + jobSchedule.getJobName(), cce);
        }
    }

    /**
     * Create the quartz scheduler lazily
     */
    protected void createQuartzScheduler() throws RepositoryException {

        if (!schedulerNode.active()) {
            logger.info("Scheduler at {} is inactive, not creating quartz scheduler", schedulerNode.getNode().getPath());
            return;
        }

        if (quartzScheduler == null) {
            final Properties properties = getQuartzSchedulerConfiguration(schedulerNode.getNode());

            logger.info("Creating quartz scheduler from node {} with properties {}", schedulerNode.getNode().getPath(), properties);
            final StdSchedulerFactory factory;
            try {
                factory = new JCRSchedulerFactory(session);
                factory.initialize(properties);

                quartzScheduler = factory.getScheduler();
                quartzScheduler.start();
            } catch (SchedulerException e) {
                logger.error("Error creating quartz scheduler", e);
            }
        }
    }

    protected void destroyQuartzScheduler() {

        logger.info("Destroying quartz scheduler " + quartzScheduler);

        if (quartzScheduler != null) {
            try {
                quartzScheduler.shutdown();
                quartzScheduler = null;
            } catch (SchedulerException e) {
                logger.error("Error shutting down quartzScheduler", e);
            }
        }
    }

    protected String getDateString() {
        Calendar now = Calendar.getInstance();
        return dateFormat.format(now.getTime());
    }


    private PropertyResourceBundle getProjectProperties() throws IOException {
        InputStream propertiesStream;
        final String projectPropertiesPath = System.getProperty(SCHEDULER_PROPERTIES);
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
