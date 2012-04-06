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

package org.onehippo.forge.utilities.repository.scheduler.tester;

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
 * Tester job
 */
public class TesterJob implements Job {
    private static final Logger logger = LoggerFactory.getLogger(TesterJob.class);

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
