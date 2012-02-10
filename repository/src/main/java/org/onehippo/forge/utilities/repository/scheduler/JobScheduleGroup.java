package org.onehippo.forge.utilities.repository.scheduler;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.RepositoryException;
import java.util.LinkedList;
import java.util.List;

public class JobScheduleGroup {

    private List<JobSchedule> jobSchedules = new LinkedList<JobSchedule>();

    private boolean active;
    private String groupName;

    public JobScheduleGroup(final Node node) throws RepositoryException {
        super();

        fromNode(node);
    }

    public boolean active(){
        return active;
    }

    public String getGroupName(){
        return groupName;
    }

    public List<JobSchedule> getJobSchedules() {
        return jobSchedules;
    }

    /**
     * Load the job schedule group from a node of type {@link org.onehippo.scheduler.daemon.Namespace.NodeType#JOB_SCHEDULE_GROUP}.
     */
    protected void fromNode(final Node node) throws RepositoryException {

        if (!node.isNodeType(Namespace.NodeType.JOB_SCHEDULE_GROUP)) {
            throw new IllegalStateException(String.format("node %s is not of type %s but %s", node.getPath(),
                    Namespace.NodeType.JOB_SCHEDULE_GROUP, ((node == null) ? "null" : node.getPrimaryNodeType())));
        }

        active = NodeUtils.getBoolean(node, Namespace.Property.ACTIVE);

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
}
