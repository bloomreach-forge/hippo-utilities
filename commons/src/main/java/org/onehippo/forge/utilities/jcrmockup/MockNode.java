/*
 * Copyright 2011 Hippo
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.onehippo.forge.utilities.jcrmockup;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.Value;
import javax.jcr.nodetype.NodeType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

import org.mockito.Matchers;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

/**
 * JAXB annotated backing mock node for javax.jcr.Node
 */
@XmlRootElement(name = "node", namespace = MockNode.HTTP_WWW_JCP_ORG_JCR_SV_1_0)
public class MockNode {

    public static final String HTTP_WWW_JCP_ORG_JCR_SV_1_0 = "http://www.jcp.org/jcr/sv/1.0";

    // initialized with the first node mockup
    private static MockNode rootMockNode;
    private static Session session = Mockito.mock(Session.class);

    @XmlTransient
    private boolean removed = false;

    @XmlTransient
    private MockNode parent;

    @XmlElement(name = "node", namespace = MockNode.HTTP_WWW_JCP_ORG_JCR_SV_1_0)
    private List<MockNode> childNodes;

    @XmlElement(name = "property", namespace = MockNode.HTTP_WWW_JCP_ORG_JCR_SV_1_0)
    private List<MockProperty> properties;

    @XmlTransient
    private boolean propertiesMapInitialized = false;

    @XmlTransient
    private Map<String, MockProperty> propertiesMap = new HashMap<String, MockProperty>();

    @XmlAttribute(name = "name", namespace = MockNode.HTTP_WWW_JCP_ORG_JCR_SV_1_0)
    private String name;

    public List<MockNode> getMockChildNodes() {
        if (childNodes == null) {
            return Collections.emptyList();
        }
        List<MockNode> notRemovedChildNodes = new ArrayList<MockNode>();
        Iterator<MockNode> childIterator = childNodes.iterator();
        while (childIterator.hasNext()) {
            MockNode child = childIterator.next();
            if (child.isRemoved()) {
                childIterator.remove();
            } else {
                notRemovedChildNodes.add(child);
            }
        }
        return notRemovedChildNodes;
    }

    public void addMockChildNode(MockNode childNode) {
        if (this.childNodes == null) {
            this.childNodes = new ArrayList<MockNode>();
        }
        childNode.setParent(this);
        this.childNodes.add(childNode);
    }

    /**
     * The loaded mock node is initialized with a list by JAXB, however a node can only have one property of a certain
     * name. To ensure this and improve performance the initial list ist backed in a map.
     */
    private void initializePropertiesMap() {
        if (!propertiesMapInitialized && this.properties != null) {
            final Iterator<MockProperty> propertyIterator = this.properties.iterator();
            while (propertyIterator.hasNext()) {
                final MockProperty mockProperty = propertyIterator.next();
                if (mockProperty.isRemoved()) {
                    propertyIterator.remove();
                } else {
                    this.propertiesMap.put(mockProperty.getMockPropertyName(), mockProperty);
                }
            }
            propertiesMapInitialized = true;
        }
    }

    public MockProperty getMockProperty(String name) {
        initializePropertiesMap();
        if (!this.propertiesMap.containsKey(name)) {
            return null;
        }
        final MockProperty mockProperty = this.propertiesMap.get(name);
        if (mockProperty.isRemoved()) {
            this.propertiesMap.remove(name);
            return null;
        }
        return mockProperty;
    }

    public Collection<MockProperty> getMockProperties() {
        initializePropertiesMap();
        final List<MockProperty> result = new ArrayList<MockProperty>();
        final Iterator<MockProperty> mockPropertyIterator = this.propertiesMap.values().iterator();
        while (mockPropertyIterator.hasNext()) {
            final MockProperty mockProperty = mockPropertyIterator.next();
            if (mockProperty.isRemoved()) {
                mockPropertyIterator.remove();
            } else {
                result.add(mockProperty);
            }
        }
        return result;
    }

    public MockNode getMockChildNode(String name) {
        if (childNodes == null) {
            return null;
        }
        Iterator<MockNode> childIterator = childNodes.iterator();
        while (childIterator.hasNext()) {
            MockNode child = childIterator.next();
            if (child.isRemoved()) {
                childIterator.remove();
            } else if (name.equals(child.getMockNodeName())) {
                return child;
            }
        }
        return null;
    }

    public List<MockNode> getMockChildNodesByName(String name) {
        if (childNodes == null) {
            return Collections.emptyList();
        }
        List<MockNode> foundNodes = new ArrayList<MockNode>();
        Iterator<MockNode> childIterator = childNodes.iterator();
        while (childIterator.hasNext()) {
            MockNode child = childIterator.next();
            if (child.isRemoved()) {
                childIterator.remove();
            } else if (name.equals(child.getMockNodeName())) {
                foundNodes.add(child);
            }
        }
        return foundNodes;
    }

    public void setMockProperty(MockProperty property) {
        initializePropertiesMap();
        property.setParent(this);
        this.propertiesMap.put(property.getMockPropertyName(), property);
    }

    public String getMockNodeName() {
        return name;
    }

    public void setMockNodeName(String name) {
        this.name = name;
    }

    public boolean isRemoved() {
        return removed;
    }

    public void setRemoved(final boolean removed) {
        this.removed = removed;
    }

    public MockNode getParent() {
        return parent;
    }

    public void setParent(MockNode parent) {
        this.parent = parent;
    }

    public String getPath() {
        if (MockNode.getRooMockNode() == this) {
            return "/";
        }
        int index = getIndex();
        if (this.parent == rootMockNode) {
            if (index > 1) {
                return "/"+this.name+"["+index+"]";
            }
            return "/" + this.name;
        } else {
            if (index > 1) {
                return this.parent.getPath() + "/" + this.name+"["+index+"]";
            }
            return this.parent.getPath() + "/" + this.name;
        }
    }

    public void buildTree(final MockNode parent) {
        this.parent = parent;
        if (this.properties != null) {
            for (MockProperty property : this.properties) {
                property.setParent(this);
            }
        }
        if (childNodes != null) {
            for (MockNode childNode : childNodes) {
                childNode.buildTree(this);
            }
        }
    }

    public int getIndex() {
        if (this.parent == null) {
            return 0;
        }
        int index = 1;
        for (MockNode sibling : this.parent.childNodes) {
            if (this.name.equals(sibling.name)) {
                if (this.equals(sibling)) {
                    return index;
                }
                index++;
            }
        }
        return 0;
    }

    public Node getJcrMock() throws RepositoryException {
        return mockJcrNode(this);
    }

    /**
     * Mocks a javax.jcr.Node by backing a MockNode.
     *
     * @param mockNode the node to mock as javax.jcr.Node
     * @return the mocked jcr node
     * @throws RepositoryException if mocking the node fails
     */
    public static Node mockJcrNode(final MockNode mockNode) throws RepositoryException {
        Node jcrNode = Mockito.mock(Node.class);
        Mockito.when(jcrNode.getName()).thenReturn(mockNode.getMockNodeName());

        mockUuid(mockNode, jcrNode);

        mockMixins(mockNode, jcrNode);

        final Answer<NodeIterator> nodeIteratorAnswer = new NodeIteratorAnswer(mockNode);
        Mockito.when(jcrNode.getNodes(Matchers.anyString())).thenAnswer(nodeIteratorAnswer);
        Mockito.when(jcrNode.getNodes()).thenAnswer(nodeIteratorAnswer);

        Mockito.when(jcrNode.getProperties()).thenAnswer(new PropertyIteratorAnswer(mockNode));

        final ItemAnswer itemAnswer = new ItemAnswer(mockNode);
        Mockito.when(jcrNode.getNode(Matchers.anyString())).thenAnswer(itemAnswer);
        Mockito.when(jcrNode.getProperty(Matchers.anyString())).thenAnswer(itemAnswer);
        Mockito.when(jcrNode.setProperty(Matchers.anyString(), Matchers.anyLong())).thenAnswer(itemAnswer);
        Mockito.when(jcrNode.setProperty(Matchers.anyString(), Matchers.anyDouble())).thenAnswer(itemAnswer);
        Mockito.when(jcrNode.setProperty(Matchers.anyString(), Matchers.anyString())).thenAnswer(itemAnswer);
        Mockito.when(jcrNode.setProperty(Matchers.anyString(), Matchers.anyBoolean())).thenAnswer(itemAnswer);
        Mockito.when(jcrNode.setProperty(Matchers.anyString(), Matchers.any(String[].class))).thenAnswer(itemAnswer);
        Mockito.when(jcrNode.setProperty(Matchers.anyString(), Matchers.any(Calendar.class))).thenAnswer(itemAnswer);
        Mockito.when(jcrNode.setProperty(Matchers.anyString(), Matchers.any(Value.class))).thenAnswer(itemAnswer);
        Mockito.when(jcrNode.setProperty(Matchers.anyString(), Matchers.any(Value[].class))).thenAnswer(itemAnswer);
        Mockito.when(jcrNode.addNode(Matchers.anyString())).thenAnswer(itemAnswer);
        Mockito.when(jcrNode.addNode(Matchers.anyString(), Matchers.anyString())).thenAnswer(itemAnswer);
        Mockito.when(jcrNode.getParent()).thenAnswer(itemAnswer);
        Mockito.doAnswer(new Answer() {
            public Object answer(final InvocationOnMock invocationOnMock) {
                mockNode.setRemoved(true);
                return null;
            }
        }).when(jcrNode).remove();

        Mockito.when(jcrNode.getPath()).thenAnswer(new Answer<String>() {
            public String answer(final InvocationOnMock invocationOnMock) {
                return mockNode.getPath();
            }
        });

        final UnsupportedOperationException unsupportedOperation = new UnsupportedOperationException("The method getProperties(pattern) is not supported yet.");
        Mockito.doThrow(unsupportedOperation).when(jcrNode).getProperties(Matchers.anyString());

        final MockProperty primaryTypeProperty = mockNode.getMockProperty("jcr:primaryType");
        Mockito.when(jcrNode.isNodeType(Matchers.anyString())).thenAnswer(new Answer<Boolean>() {
            public Boolean answer(InvocationOnMock invocationOnMock) {
                final Object args[] = invocationOnMock.getArguments();
                return primaryTypeProperty != null && args[0] instanceof String && (args[0]).equals(primaryTypeProperty.getMockValues().get(0));
            }
        });

        final NodeType primaryNodeType = Mockito.mock(NodeType.class);
        Mockito.when(primaryNodeType.getName()).thenReturn(primaryTypeProperty.getMockValues().get(0));
        Mockito.when(jcrNode.getPrimaryNodeType()).thenReturn(primaryNodeType);

        Mockito.when(jcrNode.hasProperty(Matchers.anyString())).thenAnswer(new Answer<Boolean>() {
            public Boolean answer(InvocationOnMock invocationOnMock) {
                final Object args[] = invocationOnMock.getArguments();
                return mockNode.getMockProperty((String) args[0]) != null;
            }
        });
        Mockito.when(jcrNode.hasNode(Matchers.anyString())).thenAnswer(new Answer<Boolean>() {
            public Boolean answer(InvocationOnMock invocationOnMock) {
                final Object args[] = invocationOnMock.getArguments();
                return mockNode.getMockChildNode((String) args[0]) != null;
            }
        });
        Mockito.when(jcrNode.getIndex()).thenAnswer(new Answer<Integer>() {
            public Integer answer(final InvocationOnMock invocationOnMock) {
                return mockNode.getIndex();
            }
        });

        if (rootMockNode == null) {
            // initialize with the first node mock
            rootMockNode = mockNode;
            session = Mockito.mock(Session.class);
            Mockito.when(session.getItem(Matchers.anyString())).thenAnswer(itemAnswer);
            Mockito.when(session.getRootNode()).thenAnswer(itemAnswer);
            Mockito.when(session.itemExists(Matchers.anyString())).thenAnswer(new Answer<Boolean>() {
                public Boolean answer(final InvocationOnMock invocationOnMock) throws RepositoryException {
                    return itemAnswer.getItem(invocationOnMock.getArguments()) != null;
                }
            });
        }

        Mockito.when(jcrNode.getSession()).thenReturn(session);

        return jcrNode;
    }

    private static void mockMixins(MockNode mockNode, Node jcrNode) throws RepositoryException {
        final MockProperty mixinProperty = mockNode.getMockProperty("jcr:mixinTypes");
        if (mixinProperty != null) {
            final List<String> values = mixinProperty.getMockValues();
            final List<NodeType> nodeTypes = new ArrayList<NodeType>();
            for (String value : values) {
                final NodeType nodeType = Mockito.mock(NodeType.class);
                Mockito.when(nodeType.getName()).thenReturn(value);
                nodeTypes.add(nodeType);
            }
            Mockito.when(jcrNode.getMixinNodeTypes()).thenReturn(nodeTypes.toArray(new NodeType[nodeTypes.size()]));
        }
    }

    private static void mockUuid(MockNode mockNode, Node jcrNode) throws RepositoryException {
        final MockProperty uuidProperty = mockNode.getMockProperty("jcr:uuid");
        if (uuidProperty != null) {
            final List<String> values = uuidProperty.getMockValues();
            if (values.size() == 1) {
                Mockito.when(jcrNode.getUUID()).thenReturn(values.get(0));
            }
        }
    }

    /**
     * Invalidates the session for all nodes.
     */
    public static void invalidateSession() {
        rootMockNode = null;
        session = Mockito.mock(Session.class);
    }

    public static MockNode getRooMockNode() {
        return rootMockNode;
    }
}
