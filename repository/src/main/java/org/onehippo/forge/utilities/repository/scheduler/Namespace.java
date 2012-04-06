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

/**
 * Scheduler namespace constants.
 */
public final class Namespace {

    private Namespace() {
        // hidden constructor
    }

    public static final String NAMESPACE = "scheduler";
    public static final String NAMESPACE_PREFIX = NAMESPACE + ":";

    public final static class NodeType {
        public static final String JOB_SCHEDULE = NAMESPACE_PREFIX + "jobSchedule";
        public static final String JOB_SCHEDULE_GROUP = NAMESPACE_PREFIX + "jobScheduleGroup";
        public static final String SCHEDULER = NAMESPACE_PREFIX + "scheduler";
        public static final String JOB_CONFIGURATION = NAMESPACE_PREFIX + "jobConfiguration";
    }

    public class Property {
        public static final String ACTIVE = NAMESPACE_PREFIX + "active";
        public static final String CRON_EXPRESSION = NAMESPACE_PREFIX + "cronExpression";
        public static final String CRON_EXPRESSION_DESCRIPTION = NAMESPACE_PREFIX + "cronExpressionDescription";
        public static final String JOB_CLASS_NAME = NAMESPACE_PREFIX + "jobClassName";
        public static final String RUN_INSTANTLY = NAMESPACE_PREFIX + "runInstantly";
    }

    public class Node {
        public static final String JOB_CONFIGURATION = NAMESPACE_PREFIX + "jobConfiguration";
    }
}
