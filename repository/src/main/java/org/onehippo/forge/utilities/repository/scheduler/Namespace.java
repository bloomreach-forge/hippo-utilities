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
