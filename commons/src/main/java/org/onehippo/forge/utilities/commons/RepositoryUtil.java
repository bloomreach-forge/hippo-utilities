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

package org.onehippo.forge.utilities.commons;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.jcr.Item;
import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.Property;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

import org.hippoecm.repository.HippoRepository;
import org.hippoecm.repository.HippoRepositoryFactory;
import org.hippoecm.repository.api.HippoNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @version $Id: RepositoryUtil.java 102838 2011-01-12 11:44:43Z mdenburger $
 */
public final class RepositoryUtil {

    private static Logger log = LoggerFactory.getLogger(RepositoryUtil.class);

    public static final String FOLDER_TYPE = "hippostd:folder";

    public static final Map<String, Object> DEFAULT_FOLDER_PROPERTIES = new HashMap<String, Object>();

    static {
        DEFAULT_FOLDER_PROPERTIES.put("hippostd:foldertype", new String[]{"new-folder", "new-document"});
    }

    /**
     * Private constructor preventing instantiation.
     */
    private RepositoryUtil() {
    }

    /**
     * Get a session from a new login into a HippoRepository.
     */
    public static Session getSession(final String repoConnection, final String userName, final String password)
            throws RepositoryException {
        final HippoRepository repository = HippoRepositoryFactory.getHippoRepository(repoConnection);
        return repository.login(userName, password.toCharArray());
    }

    public static void removeNode(Session session, final String fullPath) throws RepositoryException {
        HippoNode node = (HippoNode) session.getItem(fullPath);
        Node parent = node.getParent();
        node.remove();
        parent.save();
    }

    public static Node getNodeByPath(Session session, String absPath) throws RepositoryException {
        Item item = session.getItem(absPath);
        if (item instanceof Node) {
            return (Node) item;
        }
        return null;
    }

    public static Node ensureParent(Node parent, String relFolderPath) throws RepositoryException {
        return ensureParent(parent, relFolderPath, FOLDER_TYPE, DEFAULT_FOLDER_PROPERTIES);
    }

    public static Node ensureParent(Node parent, final String relFolderPath, final Map<String, Object> properties)
            throws RepositoryException {
        return ensureParent(parent, relFolderPath, FOLDER_TYPE, properties);
    }

    public static Node ensureParent(final Node parent, final String relFolderPath, final String folderType,
                                    final Map<String, Object> properties) throws RepositoryException {
        final List<String> folders = folderParts(relFolderPath);
        Node tmp = parent;
        for (String folder : folders) {
            tmp = createFolder(tmp, folderType, folder, properties);
        }
        return tmp;
    }

    /**
     * For given path, create a list of separate node names.
     *
     * @param folderPath repository folder path, like {@code  /foo/bar/path/}
     * @return folder names, like {@code foo,bar,path}
     */
    public static List<String> folderParts(final String folderPath) {
        if (folderPath.indexOf('/') == -1) {
            return Collections.emptyList();
        }
        final List<String> folders = new ArrayList<String>();
        final String[] parts = folderPath.split("/");
        for (String part : parts) {
            if (part.trim().length() != 0) {
                folders.add(part);
            }
        }
        return folders;
    }

    public static Node createFolder(Node parent, final String type, final String folderName,
                                    Map<String, Object> properties) throws RepositoryException {
        if (parent.hasNode(folderName)) {
            return parent.getNode(folderName);
        }

        final Node node = parent.addNode(folderName, type);
        for (Map.Entry<String, Object> entry : properties.entrySet()) {
            Object obj = entry.getValue();
            if (obj instanceof String[]) {
                node.setProperty(entry.getKey(), (String[]) obj);
            } else if (obj instanceof String) {
                node.setProperty(entry.getKey(), (String) obj);
            }
            // TODO add more types here
        }

        node.addMixin("hippo:harddocument");
        parent.save();

        return node;
    }

    public static Node createHardHandle(final Node parent, final String name, final boolean overwrite)
            throws RepositoryException {
        if (parent.hasNode(name)) {
            if (overwrite) {
                log.info("Removing existing binary document: " + name);
                parent.getNode(name).remove();
            } else {
                log.info("Binary already exists, skipping: " + name);
                return parent.getNode(name);
            }
        }
        // add handle
        Node handle = parent.addNode(name, "hippo:handle");
        handle.addMixin("hippo:hardhandle");
        return handle;
    }

    public static Node createDocumentNode(final Node parent, final String name, final String type)
            throws RepositoryException {
        // add handle
        final Node handle = parent.addNode(name, "hippo:handle");
        handle.addMixin("hippo:hardhandle");
        // add document node
        final Node document = handle.addNode(name, type);
        document.addMixin("hippo:harddocument");
        return document;
    }

    public static void setBinaryData(Node node, InputStream data, String mimeType) throws RepositoryException {
        node.setProperty("jcr:data", node.getSession().getValueFactory().createBinary(data));
        node.setProperty("jcr:mimeType", mimeType);
        node.setProperty("jcr:lastModified", Calendar.getInstance());
    }

    public static void addWorkflow(Node node) throws RepositoryException {
        node.setProperty("hippostdpubwf:createdBy", "admin");
        node.setProperty("hippostdpubwf:creationDate", Calendar.getInstance());
        node.setProperty("hippostdpubwf:lastModifiedBy", "admin");
        node.setProperty("hippostdpubwf:lastModificationDate", Calendar.getInstance());
    }

    public static Node getDocumentVariant(final Node handle, final String primaryType, final String hippoStdState) throws RepositoryException {
        if (handle == null || !handle.isNodeType("hippo:handle")) {
            return null;
        }
        final NodeIterator nodeIterator = handle.getNodes();
        while (nodeIterator.hasNext()) {
            final Node childNode = nodeIterator.nextNode();
            if (childNode != null && childNode.isNodeType(primaryType)) {
                if (hippoStdState == null) {
                    return childNode;
                }
                final Property state = childNode.getProperty("hippostd:state");
                if (state != null && hippoStdState.equals(state.getString())) {
                    return childNode;
                }
            }
        }
        return null;
    }
}