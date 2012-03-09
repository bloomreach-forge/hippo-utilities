package org.onehippo.forge.utilities.repository.scheduler;

import javax.jcr.Node;
import javax.jcr.RepositoryException;

public class JobSchedule {

    private String cronExpression;
    private String cronExpressionDescription;
    private boolean active;
    private boolean runInstantly;
    private String jobClassName;
    private String jobName;
    private String groupName;
    private JobConfiguration jobConfiguration;

    public JobSchedule(final String groupName, final Node node) throws RepositoryException {
        super();

        this.groupName = groupName;
        this.fromNode(node);
    }

    public String getCronExpression() {
        return cronExpression;
    }

    public String getCronExpressionDescription(){
        return cronExpressionDescription;
    }

    public boolean active(){
        return active;
    }

    public boolean runInstantly(){
        return runInstantly;
    }

    public String getJobName(){
        return jobName;
    }

    public String getJobClassName() {
        return jobClassName;
    }

    public String getGroupName() {
        return groupName;
    }

    public JobConfiguration getJobConfiguration() {
        return jobConfiguration;
    }

    /**
     * Load the job schedule from a node of type {@link Namespace.NodeType#JOB_SCHEDULE}.
     */
    protected void fromNode(final Node node) throws RepositoryException {
        if (!node.isNodeType(Namespace.NodeType.JOB_SCHEDULE)) {
            throw new IllegalStateException(String.format("node %s is not of type %s but %s", node.getPath(),
                    Namespace.NodeType.JOB_SCHEDULE, ((node == null) ? "null" : node.getPrimaryNodeType())));
        }

        jobName = node.getName();

        // properties
        active = NodeUtils.getBoolean(node, Namespace.Property.ACTIVE, true);
        cronExpression = NodeUtils.getString(node, Namespace.Property.CRON_EXPRESSION);
        cronExpressionDescription = NodeUtils.getString(node, Namespace.Property.CRON_EXPRESSION_DESCRIPTION);
        runInstantly = NodeUtils.getBoolean(node, Namespace.Property.RUN_INSTANTLY);
        jobClassName = NodeUtils.getString(node, Namespace.Property.JOB_CLASS_NAME);

        if (node.hasNode(Namespace.Node.JOB_CONFIGURATION)) {
            jobConfiguration = new JobConfiguration(node.getNode(Namespace.Node.JOB_CONFIGURATION));
        }
        else {
            jobConfiguration = new JobConfiguration();
        }
    }

    // for debugging and logging
    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder(JobSchedule.class.getSimpleName());
        builder.append("[groupName=").append(groupName);
        builder.append(", jobName=").append(jobName);
        builder.append(", jobClassName=").append(jobClassName);
        builder.append(", active=").append(active);
        builder.append(", runInstantly=").append(runInstantly);
        builder.append(", cronExpression=").append(cronExpression);
        builder.append(", cronExpressionDescription=").append(cronExpressionDescription);
        builder.append("]");
        return builder.toString();
    }
}
