package org.onehippo.forge.utilities.repository.scheduler.test;

import javax.jcr.Session;

import org.onehippo.forge.utilities.repository.scheduler.JobConfiguration;
import org.onehippo.forge.utilities.repository.scheduler.jcr.JCRScheduler;
import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Test job
 */
public class TestJob implements Job {
    private static final Logger logger = LoggerFactory.getLogger(TestJob.class);

    @Override
    public void execute(final JobExecutionContext context) throws JobExecutionException {

        logger.info("executing job {}", this);
        if (!(context.getScheduler() instanceof JCRScheduler)) {
            logger.error("Scheduler is not a {} but {}", JCRScheduler.class.getName(),
                    (context.getScheduler() == null) ? "null" : context.getScheduler().getClass().getName()
                + ". NOT executed " + this.getClass().getName());
            return;
        }

        JCRScheduler jcrScheduler = (JCRScheduler) context.getScheduler();

        // get session from jcrScheduling context!
        final Session session = jcrScheduler.getJCRSchedulingContext().getSession();
        logger.info("Session retrieved from JCRSchedulingContext: session={}", session);

        final JobDataMap dataMap = context.getMergedJobDataMap();
        final JobConfiguration jobConfiguration = (JobConfiguration) dataMap.get(JobConfiguration.class.getSimpleName());
        logger.info("data map attribute {}: {}", JobConfiguration.class.getSimpleName(), jobConfiguration);

        logger.info("jobConfiguration(test.boolean)={}", jobConfiguration.getBoolean("test.boolean"));
        logger.info("jobConfiguration(test.date)={}", jobConfiguration.getDate("test.date"));
        logger.info("jobConfiguration(test.double)={}", jobConfiguration.getDouble("test.double"));
        logger.info("jobConfiguration(test.long)={}", jobConfiguration.getLong("test.long"));
        logger.info("jobConfiguration(test.string)={}", jobConfiguration.getString("test.string"));
        logger.info("jobConfiguration(test.string.default)={}", jobConfiguration.getString("test.string.default", "default"));
    }

    public String toString() {
        return this.getClass().getName();
    }
}
