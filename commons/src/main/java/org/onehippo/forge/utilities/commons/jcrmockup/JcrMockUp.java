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

package org.onehippo.forge.utilities.commons.jcrmockup;

import java.io.InputStream;
import java.util.Arrays;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Mock utils for JCR node tree and session.
 * <p>
 * Howto:
 * Export a branch / node / tree from the hippo cms console as xml and mock it up as a root node / with session.
 * </p>
 * <p>
 * Limitations:
 * <ul>
 *  <li>No support of patterns for getProperty and getNode</li>
 *  <li>No support of type constraints according to cnd (e. g. the property defintions is multiple when there are multiple values present)</li>
 *  <li>javax.jcr.Node#isType() doesn't support sub types</li>
 *  <li>javax.jcr.Value#setValue is not supported</li>
 *  <li>workspaces are not supported</li>
 *  <li>session.copy is not supported</li>
 * </ul>
 * </p>
 * @version $id$
 */
public final class JcrMockUp {

    private static Logger logger = LoggerFactory.getLogger(JcrMockUp.class);

    private JcrMockUp() {
        // private constructor for utility
    }

    private static MockNode mockNode(String resourceName) {
        try {
            final InputStream inputStream = JcrMockUp.class.getResourceAsStream(resourceName);
            try {
                return mockNode(inputStream);
            } finally {
                inputStream.close();
            }
        } catch (Exception exception) {
            logger.error("Error occurred mocking a node for resource: " + resourceName, exception);
        }
        return null;
    }

    private static MockNode mockNode(InputStream inputStream) {
        try {
            final JAXBContext jaxbContext = JAXBContext.newInstance(MockNode.class);
            final Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
            MockNode rootNode = (MockNode) unmarshaller.unmarshal(inputStream);
            rootNode.buildTree(rootNode);
            return rootNode;
        } catch (Exception exception) {
            logger.error("Error occurred mocking a node for input stream: " + inputStream, exception);
        }
        return null;
    }

    public static Node mockJcrNode(String resourceName) throws RepositoryException {
        MockNode.invalidateSession();
        final MockNode mockNode = mockNode(resourceName);
        if (mockNode == null) {
            return null;
        }
        return mockNode.getJcrMock();
    }

    public static Node mockJcrNode(InputStream inputStream) throws RepositoryException {
        MockNode.invalidateSession();
        final MockNode mockNode = mockNode(inputStream);
        if (mockNode == null) {
            return null;
        }
        return mockNode.getJcrMock();
    }

    public static Session mockJcrSession(String resourceName) throws RepositoryException {
        final Node jcrRootNode = mockJcrNode(resourceName);
        if (jcrRootNode == null) {
            return null;
        }
        return jcrRootNode.getSession();
    }

    public static Session mockJcrSession(InputStream inputStream) throws RepositoryException {
        final Node jcrRootNode = mockJcrNode(inputStream);
        if (jcrRootNode == null) {
            return null;
        }
        return jcrRootNode.getSession();
    }

    public static Session mockEmptySession() throws RepositoryException {
        MockNode.invalidateSession();
        final MockNode rootNode = new MockNode();
        rootNode.setMockNodeName("jcr:root");
        final MockProperty nodeTypeProperty = new MockProperty();
        nodeTypeProperty.setMockPropertyName("jcr:primaryType");
        nodeTypeProperty.setMockValues(Arrays.asList("rep:root"));
        rootNode.setMockProperty(nodeTypeProperty);
        final MockProperty mixinsTypeProperty = new MockProperty();
        mixinsTypeProperty.setMockPropertyName("jcr:mixins");
        mixinsTypeProperty.setMockValues(Arrays.asList("mix:referenceable"));
        rootNode.setMockProperty(mixinsTypeProperty);
        final MockProperty uuidProperty = new MockProperty();
        uuidProperty.setMockPropertyName("jcr:uuid");
        uuidProperty.setMockValues(Arrays.asList("cafebabe-cafe-babe-cafe-babecafebabe"));
        rootNode.setMockProperty(uuidProperty);
        final Node jcrRootNode = rootNode.getJcrMock();
        return jcrRootNode.getSession();
    }

}
