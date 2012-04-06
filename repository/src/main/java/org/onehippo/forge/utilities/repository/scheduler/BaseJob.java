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

import javax.jcr.Session;

import org.onehippo.forge.utilities.repository.scheduler.jcr.JCRScheduler;
import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Absract base job providing access to JCR session and to the job's configuration.
 */
public abstract class BaseJob implements Job {

    protected static final Logger logger = LoggerFactory.getLogger(BaseJob.class);

    /**
     * Get the job's configuration object from execution context.
     */
    protected JobConfiguration getJobConfiguration(final JobExecutionContext context) {
        final JobDataMap dataMap = context.getMergedJobDataMap();
        return (JobConfiguration) dataMap.get(JobConfiguration.class.getName());
    }

    /**
     * Get a JCR session from execution context.
     */
    protected Session getJCRSession(final JobExecutionContext context) {
        final JCRScheduler jcrScheduler = getJCRScheduler(context);
        if (jcrScheduler != null) {
            return jcrScheduler.getJCRSchedulingContext().getSession();
        }
        return null;
    }

    /**
     * Get the JCR aware scheduler from execution context
     */
    protected JCRScheduler getJCRScheduler(final JobExecutionContext context) {
        if (!(context.getScheduler() instanceof JCRScheduler)) {
            logger.error("{}: context scheduler is not a {} but {}",
                    new String[]{this.getClass().getName(),
                    JCRScheduler.class.getName(),
                    (context.getScheduler() == null) ? "null" : context.getScheduler().getClass().getName()});
            return null;
        }

        return (JCRScheduler) context.getScheduler();
    }
}
