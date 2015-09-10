/*
 * Copyright 2012-2013 Hippo B.V. (http://www.onehippo.com)
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
import javax.jcr.Property;
import javax.jcr.RepositoryException;

import org.hippoecm.repository.ext.UpdaterContext;
import org.hippoecm.repository.ext.UpdaterItemVisitor;
import org.hippoecm.repository.ext.UpdaterModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Base Updater that visits
 *  /hippo:configuration/hippo:namespaces
 *  /hippo:configuration/hippo:initialize
 *  /hst:configuration/hst:configuration
 *  /content/documents
 * ..and provides protected methods to implement changes on above visitors
 *
 * @author jhoffman
 * @author mmilicevic
 * @author jreijn
 * @deprecated Updater modules are deprecated in Hippo 7.9, use updater scripts instead.
 */
@Deprecated
public abstract class BaseUpdater implements UpdaterModule {

    protected static final Logger logger = LoggerFactory.getLogger(BaseUpdater.class);

    protected static final String PROPERTIES_PROPERTY = "properties:property";
    protected static final String PROPERTIES_NAME = "properties:name";
    protected static final String PROPERTIES_VALUE = "properties:value";

    public void register(final UpdaterContext context) {

        registerTags(context);

        // hippo:namespaces
        context.registerVisitor(new UpdaterItemVisitor.PathVisitor("/hippo:namespaces") {
            @Override
            protected void leaving(Node node, int level) throws RepositoryException {
                try {
                    updateNamespaceNode(context, node);
                }
                catch (Exception e) {
                    handleException(e, node, "hippo:namespaces");
                }
            }
        });

        // hippo:initialize
        context.registerVisitor(new UpdaterItemVisitor.PathVisitor("/hippo:configuration/hippo:initialize") {
            @Override
            protected void leaving(Node node, int level) throws RepositoryException {
                try {
                    updateInitializeNode(context, node);
                }
                catch (Exception e) {
                    handleException(e, node, "hippo:initialize");
                }
            }
        });

        // hst:configuration
        context.registerVisitor(new UpdaterItemVisitor.PathVisitor("/hst:configuration/hst:configuration") {
            @Override
            protected void leaving(Node hstNode, int level) throws RepositoryException {
                try {
                    updateHSTSitemapItems(context, hstNode.getNode("hst:sitemap"));
                    updateHSTPages(context, hstNode.getNode("hst:pages"));
                    updateHSTComponents(context, hstNode.getNode("hst:components"));
                    updateHSTTemplates(context, hstNode.getNode("hst:templates"));
                    updateHSTSitemenus(context, hstNode.getNode("hst:sitemenus"));
                }
                catch (Exception e) {
                    handleException(e, hstNode, "hst:configuration");
                }
            }
        });

        // documents
        context.registerVisitor(new UpdaterItemVisitor.PathVisitor("/content/documents") {
            @Override
            protected void leaving(Node node, int level) throws RepositoryException {
                try {
                    updateDocuments(context, node);
                }
                catch (Exception e) {
                    handleException(e, node, "documents");
                }
            }
        });

    }

    /**
     * Use this to register the <i>start</i> and <i>end</i> tag for which the updater module should execute.
     * @param context the {@link org.hippoecm.repository.ext.UpdaterContext} on which the start and end tag will be set.
     */
    protected abstract void registerTags(UpdaterContext context);

    /**
     * Remove a sub node
     * @param node the parent {@link javax.jcr.Node} from which the childNode should be removed
     * @param name the name of the child {@link javax.jcr.Node}
     * @throws javax.jcr.RepositoryException In case the child node could not be removed
     */
    protected void removeSubNode(Node node, String name) throws RepositoryException {
        logger.info("Removing subnode " + name + " of node " + node.getPath() + " (" + node.hasNode(name) + ')');
        if (node.hasNode(name)) {
            node.getNode(name).remove();
        }
    }

    /**
     * Remove a {@link javax.jcr.Property} by name from the given {@link javax.jcr.Node}.
     * @param node the from which the property should be removed
     * @param property name of the property which should be removed
     * @throws javax.jcr.RepositoryException In case the property could not be removed
     */
    protected void removeProperty(Node node, String property) throws RepositoryException {
        logger.info("Removing property " + property + " of node " + node.getPath() + " (" + node.hasNode(property)
                + ')');
        if (node.hasProperty(property)) {
            node.getProperty(property).remove();
        }
    }

    /**
     * Gets the {@link String} value of the given {@link javax.jcr.Property} from the {@link javax.jcr.Node}. This method will not work for
     * property of type binary or date.
     * @param node the {@link javax.jcr.Node}
     * @param property the name of the property
     * @return the String representation of the given property
     * @throws javax.jcr.RepositoryException In case the value of the property could not be retrieved.
     */
    protected String getProperty(Node node, String property) throws RepositoryException {

        if ((node == null) || (!node.hasProperty(property))) {
            return null;
        }

        return node.getProperty(property).getString();
    }

    /**
     * Gets the UUID of the given {@link javax.jcr.Node}
     * <strong>Note: the usage of this method is discouraged, because the uuid is unstable during the update process.
     * Only use it if you know that the UUID is not going to change.</strong>
     * @param node the Node from which to get the UUI
     * @return the String representation of the UUID.
     * @throws javax.jcr.RepositoryException In case something goes wrong while trying to get the UUID
     */
    protected String getUUID(Node node) throws RepositoryException {
        return getProperty(node, "jcr:uuid");
    }


    protected void updateNamespaceNode(final UpdaterContext context, final Node node) throws RepositoryException {
        // use JCR API
    }

    protected void updateInitializeNode(final UpdaterContext context, final Node node) throws RepositoryException {
        // use removeSubNode
    }

    protected void updateHSTPages(final UpdaterContext context, final Node pagesNode) throws RepositoryException {
        // use setHSTPage
    }

    protected void updateHSTComponents(final UpdaterContext context, final Node componentsNode) throws RepositoryException {
        // use setHSTComponent
    }

    protected void updateHSTSitemapItems(final UpdaterContext context, final Node sitemapNode) throws RepositoryException {
        // use setHSTSitemapItem
    }

    protected void updateHSTTemplates(final UpdaterContext context, final Node templatesNode) throws RepositoryException {
        // use setHSTTemplate
    }

    protected void updateHSTSitemenus(final UpdaterContext context, final Node menusNode) throws RepositoryException {
     // use setHSTSitemenu and setHSTSitemenuItem
    }

    protected void updateDocuments(final UpdaterContext context, final Node node) throws RepositoryException {
        // use JCR API
    }

    /**
     * Sets properties on an existing HST page, or will create an HST page with the provided name, reference component and
     * template.
     * @param node The parent node of the HST page.
     * @param name The name of the HST page by which it can be found or by which it should be created.
     * @param referenceComponent if not <code>null</code> the <code>hst:referencecomponent</code> property
     * @param template if not <code>null</code> the <code>hst:template</code> property will be set,
     * if <code>null</code> the existing <code>hst:template</code> property will be removed
     * @return the {@link javax.jcr.Node} containing the HST page
     * @throws javax.jcr.RepositoryException In case something goes wrong while getting or creating the node
     */
    protected Node setHSTPage(Node node, String name, String referenceComponent, String template)
            throws RepositoryException {

        logger.info("setHSTPage " + name + " (exists=" + node.hasNode(name) + "), referenceComponent="
                + referenceComponent);

        final Node page = node.hasNode(name) ? node.getNode(name) : node.addNode(name, "hst:component");
        if (referenceComponent != null) {
            page.setProperty("hst:referencecomponent", referenceComponent);
        }
        if (template != null) {
            page.setProperty("hst:template", template);
        }
        else if (page.hasProperty("hst:template")) {
            page.getProperty("hst:template").remove();
        }
        return page;
    }

    /**
     * Sets/Modifies the <code>hst:template</code> configuration
     * @param node the parent {@link javax.jcr.Node} to which to add the <code>hst:template</code> node or from which to get the
     * existing template
     * @param name the name of the existing <code>hst:template</code> or if it does not exist,
     * a new node of type <code>hst:template</code> wil be created
     * @param renderPath the <code>hst:renderpath</code> property of the template,
     * if <code>null</code> the property will not be set
     * @return the {@link javax.jcr.Node} containing the hst template configuration
     * @throws javax.jcr.RepositoryException In case the an exception occurs while trying to get/create the template node or according
     * properties.
     */
    protected Node setHSTTemplate(Node node, String name, String renderPath) throws RepositoryException {

        logger.info("setHSTTemplate " + name + " (exists=" + node.hasNode(name) + "), renderPath=" + renderPath);

        final Node template = node.hasNode(name) ? node.getNode(name) : node.addNode(name, "hst:template");
        if (renderPath != null) {
            template.setProperty("hst:renderpath", renderPath);
        }
        return template;
    }

    /**
     * Sets/Modifies the <code>hst:component</code> configuration for a single component configuration
     * @param node the parent {@link javax.jcr.Node} to which to add the <code>hst:component</code> or from which to get the
     * existing hst component
     * @param name the name of the existing <code>hst:component</code> or if it does not exist,
     * a new node of type <code>hst:component</code> wil be created with this name
     * @param componentClassName the class for the component
     * @param template if not <code>null</code> the <code>hst:template</code> property will for this component will be set
     * @param parameterNames the hst component parameter names
     * @param parameterValues the hst component parameter values
     * @return the {@link javax.jcr.Node} containing the hst component configuration
     * @throws javax.jcr.RepositoryException In case an exception occurs while trying to get/create the component node or according
     * properties.
     */
    protected Node setHSTComponent(final Node node, final String name, final String componentClassName,
            final String template, final String[] parameterNames, final String[] parameterValues)
            throws RepositoryException {

        logger.info("setHSTComponent " + name + " (exists=" + node.hasNode(name) + "), componentClassName="
                + componentClassName + ", template=" + template);

        final Node component = node.hasNode(name) ? node.getNode(name) : node.addNode(name, "hst:component");
        if (componentClassName != null) {
            component.setProperty("hst:componentclassname", componentClassName);
        }
        if (template != null) {
            component.setProperty("hst:template", template);
        }

        setHSTParameterNamesValues(component, parameterNames, parameterValues);

        return component;
    }

    /**
     * Sets/Modifies the <code>hst:component</code> configuration
     * @param node the parent {@link javax.jcr.Node} to which to add the <code>hst:sitemap</code> or from which to get the
     * existing sitemap item
     * @param name the name of the existing <code>hst:sitemap</code> or if it does not exist,
     * a new node of type <code>hst:sitemap</code> wil be created with this name
     * @param componentConfigurationId if not <code>null</code> the <code>hst:componentconfigurationid</code> property
     * will be set for this HST sitemap item.
     * @param relContentPath if not <code>null</code> the <code>hst:relativecontentpath</code> property
     * will be set for this HST sitemap item.
     * @return the {@link javax.jcr.Node} containing the hst sitemap item configuration
     * @throws javax.jcr.RepositoryException In case an exception occurs while trying to get/create the sitemap node or according
     * properties.
     */
    protected Node setHSTSitemapItem(final Node node, final String name, final String componentConfigurationId,
            final String relContentPath) throws RepositoryException {

        logger.info("setHSTSitemapItem " + name + " (exists=" + node.hasNode(name) + "), componentConfigurationId="
                + componentConfigurationId + ", relContentPath=" + relContentPath);

        final Node smi = node.hasNode(name) ? node.getNode(name) : node.addNode(name, "hst:sitemapitem");
        if (componentConfigurationId != null) {
            smi.setProperty("hst:componentconfigurationid", componentConfigurationId);
        }
        if (relContentPath != null) {
            smi.setProperty("hst:relativecontentpath", relContentPath);
        }
        return smi;
    }

    /**
     * Creates/Gets a <code>hst:sitemenu</code> node
     * @param node the parent {@link javax.jcr.Node} from which to get the sitemenu, or to which to add a sitemenu.
     * @param name the name of the existing <code>hst:sitemenu</code> node or if it does not exist,
     * a new node of type <code>hst:sitemenu</code> wil be created with this name
     * @return the {@link javax.jcr.Node} containing the hst sitemenu configuration
     * @throws javax.jcr.RepositoryException In case an exception occurs while trying to get/create the sitemenu node.
     */
    protected Node setHSTSitemenu(final Node node, final String name) throws RepositoryException {

        logger.info("setHSTSitemenu " + name + " (exists=" + node.hasNode(name) + ")");

        return node.hasNode(name) ? node.getNode(name) : node.addNode(name, "hst:sitemenu");
    }

    /**
     * Sets/creates and gets a <code>hst:sitemenuitem</code> node
     * @param node the parent {@link javax.jcr.Node} from which to get the sitemenu item, or to which to add a sitemenu item.
     * @param name the name of the existing <code>hst:sitemenuitem</code> node or if it does not exist,
     * a new node of type <code>hst:sitemenuitem</code> wil be created with this name
     * @param referenceSitemapItem if not <code>null</code> the <code>hst:referencesitemapitem</code> property
     * will be set for this HST sitemenu item.
     * @return the {@link javax.jcr.Node} containing the hst sitemenu item configuration
     * @throws javax.jcr.RepositoryException In case an exception occurs while trying to get/create the sitemenu item node
     */
    protected Node setHSTSitemenuItem(final Node node, final String name, final String referenceSitemapItem) throws RepositoryException {

        logger.info("setHSTSitemenuItem " + name + " (exists=" + node.hasNode(name) + ")");

        final Node sitemenuItem = node.hasNode(name) ? node.getNode(name) : node.addNode(name, "hst:sitemenu");

        if (referenceSitemapItem != null) {
            sitemenuItem.setProperty("hst:referencesitemapitem", referenceSitemapItem);
        }

        return sitemenuItem;
    }

    /**
     * Set the <code>hst:parameternames</code> and <code>hst:parametervalues</code> properties.
     * @param node {@link javax.jcr.Node} the node to which to add the properties
     * @param parameterNames the names of parameters
     * @param parameterValues the values of parameters
     * @throws javax.jcr.RepositoryException In case an exception occurs while trying to set properties
     */
    protected void setHSTParameterNamesValues(final Node node, final String[] parameterNames,
            final String[] parameterValues) throws RepositoryException {
        if ((parameterNames != null) && (parameterValues != null)) {
            node.setProperty("hst:parameternames", parameterNames);
            node.setProperty("hst:parametervalues", parameterValues);
        }
    }

    /**
     * Adds or modifies an existing property on a properties document.
     * @param propertiesDoc the {@link javax.jcr.Node} representing the properties document
     * @param propName the name of the property
     * @param propValue the value of the property
     * @throws javax.jcr.RepositoryException In case an exception occurs while trying to set/modify the property
     */
    protected void setPropertyNode(Node propertiesDoc, String propName, String propValue) throws RepositoryException {

        NodeIterator it = propertiesDoc.getNodes(PROPERTIES_PROPERTY);
        while (it.hasNext()) {
            Node property = it.nextNode();
            Property prop = property.getProperty(PROPERTIES_NAME);
            if ((prop != null) && (propName.equals(prop.getString()))) {
                logger.info("Setting property " + propName + " with value " + propValue + " in document "
                        + propertiesDoc.getPath());
                property.setProperty(PROPERTIES_VALUE, propValue);
                return;
            }
        }

        logger.info("Creating property " + propName + " with value " + propValue + " in document "
                + propertiesDoc.getPath());
        Node propertyNode = propertiesDoc.addNode(PROPERTIES_PROPERTY, PROPERTIES_PROPERTY);
        propertyNode.setProperty(PROPERTIES_NAME, propName);
        propertyNode.setProperty(PROPERTIES_VALUE, propValue);
    }

    /**
     * Removes a property by property name from a properties document.
     * @param propertiesDoc a properties {@link javax.jcr.Node}
     * @param propName the name of the property
     * @throws javax.jcr.RepositoryException In case an exception occurs while trying to remove the property
     */
    protected void removePropertyNode(Node propertiesDoc, String propName) throws RepositoryException {

        NodeIterator it = propertiesDoc.getNodes(PROPERTIES_PROPERTY);
        while (it.hasNext()) {
            Node property = it.nextNode();
            Property prop = property.getProperty(PROPERTIES_NAME);
            if ((prop != null) && (propName.equals(prop.getString()))) {
                logger.info("Removing property " + propName + " in document " + propertiesDoc.getPath());
                property.remove();
                return;
            }
        }
    }

    /**
     * Get a {@link javax.jcr.Node} of type <i>hippostd:folder</i> based on it's parent {@link javax.jcr.Node} and the folder name.
     * @param node the parent Node of the folder
     * @param folderName the name of the folder Node
     * @return a {@link javax.jcr.Node} with the given name of type <i>hippostd:folder</i> or <code>null</code> otherwise.
     * @throws javax.jcr.RepositoryException In case something goes wrong while trying to get the folder
     */
    protected Node getFolder(Node node, String folderName) throws RepositoryException {

        if (node == null) {
            logger.info("Can't get folder " + folderName + ": parent node is null");
            return null;
        }

        if (node.hasNode(folderName)) {
            final Node folder = node.getNode(folderName);
            if (folder.isNodeType("hippostd:folder")) {
                logger.info("Getting folder " + folderName + " below " + node.getPath());
                return folder;
            }
            else {
                logger.warn("Can't get folder " + folderName + " below " + node.getPath()
                        + " because the node already exists as type " + folder.getPrimaryNodeType().getName());
                return null;
            }
        }

        return null;
    }

    /**
     * Create a Node of type <i>hippostd:folder</i> underneath the given {@link javax.jcr.Node}. With the creation of this folder,
     * the default folder types will be set.
     * @param node the Node underneath which the folder should be created
     * @param folderName the name of the folder
     * @return the Node with the new folder attached
     * @throws javax.jcr.RepositoryException In case something goes wrong while trying to add the folder
     */
    protected Node createFolder(Node node, String folderName) throws RepositoryException {
        return createFolder(node, folderName, getDefaultFolderTypes());
    }

    /**
     * Create a Node of type <i>hippostd:folder</i> underneath the given {@link javax.jcr.Node}
     * @param node the Node underneath which the folder should be created
     * @param folderName the name of the folder
     * @param folderTypes the allowed types, which can be added to this folder
     * @return the Node with the new folder attached
     * @throws javax.jcr.RepositoryException In case something goes wrong while trying to create the folder
     */
    protected Node createFolder(Node node, String folderName, String[] folderTypes) throws RepositoryException {

        Node folder = getFolder(node, folderName);

        if (folder != null) {
            return folder;
        }

        logger.info("Creating folder " + folderName + " below " + node.getPath());

        folder = node.addNode(folderName, "hippostd:folder");
        folder.addMixin("hippo:harddocument");

        if (folderTypes != null) {
            folder.setProperty("hippostd:foldertype", folderTypes);
        }

        return folder;
    }

    /**
     * Get the default folder types
     * @return an array of Strings containing the default folder types
     */
    protected String[] getDefaultFolderTypes() {
        return new String[] {"new-folder", "new-document"};
    }

    protected void handleException(Exception exception, Node node, String name) {

        // node.getPath() can lead also to exception so catch again
        try {
            logger.error("Caught exception for visitor " + name + " and node " + node.getPath(), exception);
        }
        catch (Exception e) {
            logger.error("Caught exception for visitor " + name, exception);
        }
    }
}
