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

package org.onehippo.forge.utilities.repository.updater;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.RepositoryException;
import javax.jcr.Value;

import org.hippoecm.repository.ext.UpdaterContext;
import org.hippoecm.repository.ext.UpdaterItemVisitor;

/**
 * Base CND Updater that:
 * <p/>
 * - reloads a projects cnd
 * - visits all document and compound types and set it's specific namespace and
 * - removes common initialize items for reloading doc type config
 */
public abstract class BaseCndUpdater extends BaseUpdater {

    public void register(final UpdaterContext context) {

        super.register(context);

        // reload cnd
        context.registerVisitor(new UpdaterItemVisitor.NamespaceVisitor(context, getProjectNS(),
                getClass().getClassLoader().getResourceAsStream(getProjectCND())));
    }

    protected abstract String getProjectNS();
    protected abstract String getProjectCND();
    protected abstract String getNewProjectNS();

    protected final String getProjectPrefix() {
        return getProjectNS() + ':';
    }

    protected final String getNewProjectPrefix() {
        return getNewProjectNS() + ':';
    }

    @Override
    protected void updateNamespaceNode(final UpdaterContext context, final Node node) throws RepositoryException {

        // remove namespace node
        removeSubNode(node, getProjectNS());
    }

    @Override
    protected void updateInitializeNode(final UpdaterContext context, final Node node) throws RepositoryException {

        // remove both initializeitems that load the CND and the namespace node
        // (NB override this method if the names do not match!)
        removeSubNode(node, getProjectNS());
        removeSubNode(node, getProjectNS() + "-namespace");
    }

    /**
     * Set a single String property to a new namespace.
     * <br>
     * This is needed if the namespace of the property is different from the
     * namespace of the node.
     */
    protected void renewStringProperty(final Node node, final String propertyName) throws RepositoryException {
        if (node.hasProperty(getProjectPrefix() + propertyName)) {
            node.setProperty(getNewProjectPrefix() + propertyName, node.getProperty(getProjectPrefix() + propertyName).getString());
        }
    }

    /**
     * Set a multiple String[] property to a new namespace.
     * <br>
     * This is needed if the namespace of the property is different from the
     * namespace of the node.
     */
    protected void renewStringProperties(final Node node, final String propertyName) throws RepositoryException {
        if (node.hasProperty(getProjectPrefix() + propertyName)) {
            Value[] values = node.getProperty(getProjectPrefix() + propertyName).getValues();
            node.setProperty(getNewProjectPrefix() + propertyName, values);
        }
    }

    /**
     * Set a single Date property to a new namespace.
     * <br>
     * This is needed if the namespace of the property is different from the
     * namespace of the node.
     */
    protected void renewDateProperty(final Node node, final String propertyName) throws RepositoryException {
        if (node.hasProperty(getProjectPrefix() + propertyName)) {
            node.setProperty(getNewProjectPrefix() + propertyName, node.getProperty(getProjectPrefix() + propertyName).getDate());
        }
    }

    /**
     * Set a single Boolean property under a new namespace.
     * <br>
     * This is needed if the namespace of the property is different from the
     * namespace of the node.
     */
    protected void renewBooleanProperty(final Node node, final String propertyName) throws RepositoryException {
        if (node.hasProperty(getProjectPrefix() + propertyName)) {
            node.setProperty(getNewProjectPrefix() + propertyName, node.getProperty(getProjectPrefix() + propertyName).getBoolean());
        }
    }

    /**
     * Set a subnode to a new namespace.
     * <br>
     * This is needed if the namespace of the subnode is different from the
     * namespace of the parent node.
     */
    protected void renewSubnode(final UpdaterContext context, final Node node, final String subNodeName)
            throws RepositoryException {
        if (node.hasNode(getProjectPrefix() + subNodeName)) {
            Node subnode = node.getNode(getProjectPrefix() + subNodeName);
            context.setName(subnode, getNewProjectPrefix() + subNodeName);
        }
    }

    /**
     * Set multiple subnodes to a new namespace.
     * <br>
     * This is needed if the namespace of the subnodes is different from the
     * namespace of the parent node.
     */
    protected void renewSubnodes(final UpdaterContext context, final Node node, final String subNodeName)
            throws RepositoryException {

        NodeIterator it = node.getNodes(getProjectPrefix() + subNodeName);
        String val = getNewProjectPrefix() + subNodeName;
        while (it.hasNext()) {
            Node subnode = it.nextNode();
            context.setName(subnode, val);
        }
    }
}

