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

import org.onehippo.forge.utilities.repository.scheduler.BaseJob;
import org.onehippo.forge.utilities.repository.scheduler.JobConfiguration;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

/**
 * Tester job
 */
public class TesterJob extends BaseJob {

    @Override
    public void execute(final JobExecutionContext context) throws JobExecutionException {

        logger.info("executing job {}", this);

        // get session and output
        final Session session = this.getJCRSession(context);
        logger.info("JCR Session retrieved from context: {}", session);

        // get configuration and output
        final JobConfiguration jobConfiguration = this.getJobConfiguration(context);
        logger.info("jobConfiguration: {}", jobConfiguration);
        logger.info("jobConfiguration test.boolean={}", jobConfiguration.getBoolean("test.boolean"));
        logger.info("jobConfiguration test.date={}", jobConfiguration.getDate("test.date"));
        logger.info("jobConfiguration test.double={}", jobConfiguration.getDouble("test.double"));
        logger.info("jobConfiguration test.long={}", jobConfiguration.getLong("test.long"));
        logger.info("jobConfiguration test.string={}", jobConfiguration.getString("test.string"));
        logger.info("jobConfiguration test.string.default={}", jobConfiguration.getString("test.string.default", "default"));
    }

    public String toString() {
        return this.getClass().getName();
    }
}
