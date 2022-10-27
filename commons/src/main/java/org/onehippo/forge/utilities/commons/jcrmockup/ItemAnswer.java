/*
 * Copyright 2012-2022 Bloomreach
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

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;

import javax.jcr.Item;
import javax.jcr.Node;
import javax.jcr.PathNotFoundException;
import javax.jcr.PropertyType;
import javax.jcr.RepositoryException;
import javax.jcr.Value;

import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

/**
 * Item answer for getNode, getProperty, setProperty, addNode, getItem
 */
public class ItemAnswer implements Answer<Item> {

    private static final String DATE = "Date";
    private MockNode mockNode;

    public ItemAnswer(final MockNode mockNode) {
        this.mockNode = mockNode;
    }

    public Item answer(InvocationOnMock invocation) throws RepositoryException {
        final String methodName = invocation.getMethod().getName();
        final Object args[] = invocation.getArguments();
        if ("getNode".equals(methodName)) {
            return getNode(args);
        } else if ("getProperty".equals(methodName)) {
            return getProperty(args);
        } else if ("setProperty".equals(methodName)) {
            return setProperty(args);
        } else if ("addNode".equals(methodName)) {
            return addNode(args);
        } else if ("getItem".equals(methodName)) {
            return getItem(args);
        } else if ("getParent".equals(methodName)) {
            return getParent();
        } else if ("getRootNode".equals(methodName)) {
            return MockNode.getRooMockNode().getJcrMock();
        }
        throw new UnsupportedOperationException("The method " + methodName + " is not supported");
    }

    private Node getParent() throws RepositoryException {
        return mockNode.getParent().getJcrMock();
    }

    private Item getProperty(Object args[]) throws RepositoryException {
        if (args.length == 1 && args[0] instanceof String) {
            final MockProperty mockProperty = mockNode.getMockProperty((String) args[0]);
            if (mockProperty != null) {
                return mockProperty.mockJcrProperty();
            }
        }
        throw new PathNotFoundException("Property '" + args[0] + "' does not exist");
    }

    private Item getNode(Object args[]) throws RepositoryException {
        if (args.length == 1 && args[0] instanceof String) {
            final MockNode child = mockNode.getMockChildNode((String) args[0]);
            if (child != null) {
                return MockNode.mockJcrNode(child);
            }
        }
        throw new PathNotFoundException("Node '" + args[0] + "' does not exist");
    }

    public Item getItem(Object args[]) throws RepositoryException {
        if (args.length == 1 && args[0] instanceof String) {
            if (!((String) args[0]).startsWith("/")) {
                throw new PathNotFoundException("Path '" + args[0] + "' cannot be found");
            }
            final String[] paths = splitString((String) args[0], "/");
            int pos = 0;
            MockNode childNode = mockNode.getMockChildNode(paths[pos++]);
            if (childNode == null) {
                throw new PathNotFoundException("Path '" + args[0] + "' cannot be found");
            }
            MockNode parentNode = childNode;
            while (pos < paths.length) {
                childNode = childNode.getMockChildNode(paths[pos]);
                // check if property exists
                if (childNode == null) {
                    MockProperty mockProperty = parentNode.getMockProperty(paths[pos]);
                    if (mockProperty != null) {
                        return mockProperty.mockJcrProperty();
                    }
                    break;
                }
                parentNode = childNode;
                pos++;
            }
            if (childNode != null) {
                return MockNode.mockJcrNode(childNode);
            }
        }
        throw new PathNotFoundException("Path '" + args[0] + "' cannot be found.");
    }

    private Item addNode(Object[] args) throws RepositoryException {
        if (args.length > 0 && args[0] instanceof String) {
            MockNode newChild = new MockNode();
            newChild.setMockNodeName((String) args[0]);
            if (args.length == 2 && args[1] instanceof String) {
                final MockProperty typeProperty = new MockProperty();
                typeProperty.setMockValues(Collections.singletonList((String) args[1]));
                typeProperty.setMockPropertyName("jcr:primaryType");
                newChild.setMockProperty(typeProperty);
            }
            mockNode.addMockChildNode(newChild);
            return MockNode.mockJcrNode(newChild);
        }
        return null;
    }

    private Item setProperty(Object[] args) throws RepositoryException {
        final MockProperty mockProperty = new MockProperty();
        mockProperty.setMockPropertyName((String) args[0]);
        mockProperty.setMockPropertyType(args[1].getClass().getSimpleName());
        final ArrayList<String> values = new ArrayList<String>();
        if (args[1] instanceof Calendar) {
            values.add(ISO8601.format((Calendar) args[1]));
            mockProperty.setMockPropertyType(DATE);
        } else if (args[1] instanceof Value) {
            final Object object = getValue((Value) args[1]).toString();
            values.add(object.toString());
            mockProperty.setMockPropertyType(object.getClass().getSimpleName());
        } else if (args[1] instanceof Value[]) {
            for (Value value : (Value[]) args[1]) {
                final Object object = getValue(value).toString();
                values.add(object.toString());
                mockProperty.setMockPropertyType(object.getClass().getSimpleName());
            }
        } else if (args[1].getClass().isArray()) {
            for (Object obj : (Object[]) args[1]) {
                values.add(obj.toString());
                mockProperty.setMockPropertyType(obj.getClass().getSimpleName());
            }
        } else {
            values.add(args[1].toString());
        }
        mockProperty.setMockValues(values);
        mockNode.setMockProperty(mockProperty);
        return mockProperty.mockJcrProperty();
    }

    private static String[] splitString(final String string, final String separator) {
        if (isBlank(string)) {
            return new String[0];
        }
        final List<String> valueList = new ArrayList<String>();
        final String[] values = string.split(separator);
        for (String value : values) {
            final String trimmedValue = value.trim();
            if (!isBlank(trimmedValue)) {
                valueList.add(trimmedValue);
            }
        }
        return valueList.toArray(new String[valueList.size()]);
    }

    private static boolean isBlank(String string) {
        return string == null || string.trim().length() == 0;
    }

    public static Object getValue(final Value value) throws RepositoryException {
        final int valueType = value.getType();
        switch (valueType) {
            case PropertyType.BOOLEAN:
                return value.getBoolean();
            case PropertyType.DATE:
                return value.getDate();
            case PropertyType.DOUBLE:
                return value.getDouble();
            case PropertyType.LONG:
                return value.getLong();
            case PropertyType.STRING:
                return value.getString();
        }
        throw new UnsupportedOperationException("The value type '" + valueType + "' is not supported.");
    }
}
