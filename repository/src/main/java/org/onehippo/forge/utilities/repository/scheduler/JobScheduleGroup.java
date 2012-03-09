package org.onehippo.forge.utilities.repository.scheduler;

import java.util.LinkedList;
import java.util.List;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.RepositoryException;

public class JobScheduleGroup {

    private final List<JobSchedule> jobSchedules = new LinkedList<JobSchedule>();

    private boolean active;
    private String groupName;

    public JobScheduleGroup(final Node node) throws RepositoryException {
        super();

        fromNode(node);
    }

    public boolean active() {
        return active;
    }

    public String getGroupName(){
        return groupName;
    }

    public List<JobSchedule> getJobSchedules() {
        return jobSchedules;
    }

    /**
     * Load the job schedule group from a node of type {@link Namespace.NodeType#JOB_SCHEDULE_GROUP}.
     */
    protected void fromNode(final Node node) throws RepositoryException {

        if (!node.isNodeType(Namespace.NodeType.JOB_SCHEDULE_GROUP)) {
            throw new IllegalStateException(String.format("node %s is not of type %s but %s", node.getPath(),
                    Namespace.NodeType.JOB_SCHEDULE_GROUP, ((node == null) ? "null" : node.getPrimaryNodeType())));
        }

        active = NodeUtils.getBoolean(node, Namespace.Property.ACTIVE, true);

        groupName = node.getName();

        // load job schedules
        final NodeIterator iterator = node.getNodes();
        while (iterator.hasNext()) {
            final Node subNode = iterator.nextNode();
            if (subNode.isNodeType(Namespace.NodeType.JOB_SCHEDULE)) {
                jobSchedules.add(new JobSchedule(groupName, subNode));
            }
        }
    }

    // for debugging and logging
    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder(JobScheduleGroup.class.getSimpleName());
        builder.append("[groupName=").append(groupName);
        builder.append(", active=").append(active);
        builder.append("]");
        return builder.toString();
    }
}
