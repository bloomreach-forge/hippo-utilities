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

import java.util.LinkedList;
import java.util.List;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.RepositoryException;

/**
 * Object representing a scheduler:scheduler node.
 */
public class SchedulerNode {

    private boolean active;
    private final List<JobScheduleGroup> jobScheduleGroups = new LinkedList<JobScheduleGroup>();
    private Node node;

    public SchedulerNode(final Node node) throws RepositoryException {
        super();

        this.node = node;
        fromNode(node);
    }

    /**
     * Is this scheduler active?
     */
    public boolean active() {
        return active;
    }

    /**
     * Get the JCR node of the scheduler
     */
    public Node getNode() throws RepositoryException {
        return node;
    }

    /**
     * Get the list of job schedule groups
     */
    public List<JobScheduleGroup> getJobScheduleGroups() {
        return jobScheduleGroups;
    }

    /**
     * Reload the groupd
     */
    public void reload() throws RepositoryException {
        fromNode(node);
    }

    /**
     * Load the job schedule groups from a node of type {@link Namespace.NodeType#SCHEDULER}.
     */
    protected void fromNode(final Node node) throws RepositoryException {

        if (!node.isNodeType(Namespace.NodeType.SCHEDULER)) {
            throw new IllegalStateException(String.format("node %s is not of type %s but %s", node.getPath(),
                    Namespace.NodeType.SCHEDULER, ((node == null) ? "null" : node.getPrimaryNodeType())));
        }

        active = NodeUtils.getBoolean(node, Namespace.Property.ACTIVE, true);

        // load job schedule groups
        jobScheduleGroups.clear();
        final NodeIterator iterator = node.getNodes();
        while (iterator.hasNext()) {
            final Node groupNode = iterator.nextNode();
            if (groupNode.isNodeType(Namespace.NodeType.JOB_SCHEDULE_GROUP)) {
                jobScheduleGroups.add(new JobScheduleGroup(groupNode));
            }
        }
    }
}
