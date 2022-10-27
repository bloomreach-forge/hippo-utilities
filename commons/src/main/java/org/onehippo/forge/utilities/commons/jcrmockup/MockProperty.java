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
import java.util.List;

import javax.jcr.Item;
import javax.jcr.Property;
import javax.jcr.PropertyType;
import javax.jcr.RepositoryException;
import javax.jcr.Value;
import javax.jcr.nodetype.PropertyDefinition;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlTransient;

import org.mockito.Matchers;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

/**
 * JAXB annotated backed javax.jcr.Property
 */
public class MockProperty {

    @XmlTransient
    private boolean removed;

    @XmlTransient
    private MockNode parent;   // NOSONAR (ignore field not initialized in constructor, null is valid value)

    @XmlAttribute(name = "name", namespace = "http://www.jcp.org/jcr/sv/1.0")
    private String name;       // NOSONAR (ignore field not initialized in constructor, set by XStream)

    @XmlAttribute(name = "type", namespace = "http://www.jcp.org/jcr/sv/1.0")
    private String type;       // NOSONAR (ignore field not initialized in constructor, set by XStream)

    @XmlElement(name = "value", namespace = "http://www.jcp.org/jcr/sv/1.0")
    private List<String> values;  // NOSONAR (ignore field not initialized in constructor, null is valid value)

    public String getMockPropertyName() {
        return this.name;
    }

    public void setMockPropertyName(String name) {
        this.name = name;
    }

    public String getMockPropertyType() {
        return type;
    }

    public void setMockPropertyType(String type) {
        this.type = type;
    }

    public List<String> getMockValues() {
        return values;
    }

    public void setMockValues(List<String> values) {
        this.values = values;
    }

    public MockNode getParent() {
        return this.parent;
    }

    public void setParent(MockNode parentNode) {
        this.parent = parentNode;
    }

    public String getPath() {
        if (this.parent == null) {
            return this.name;
        } else {
            StringBuilder b = new StringBuilder(this.parent.getPath());
            b.append('/');
            b.append(this.name);
            return b.toString();
        }
    }

    public void setRemoved(boolean removed) {
        this.removed = removed;
    }

    public boolean isRemoved() {
        return this.removed;
    }

    /**
     * Gets the mocked jcr property
     *
     * @return the mocked jcr property backed by this object.
     * @throws RepositoryException if mocking the property fails
     */
    public Property mockJcrProperty() throws RepositoryException {
        final MockProperty mockProperty = this;
        final Property jcrProperty = Mockito.mock(Property.class);
        Mockito.when(jcrProperty.getName()).thenReturn(this.name);
        Mockito.when(jcrProperty.getPath()).thenAnswer(new PathAnswer(mockProperty));
        Mockito.when(jcrProperty.getParent()).thenAnswer(new ParentAnswer(mockProperty));

        mockType(jcrProperty);
        if (this.values != null) {
            if (this.values.size() == 1) {
                mockSingleValueProperty(jcrProperty);
            } else if (this.values.size() > 1) {
                mockMultiValueProperty(jcrProperty);
            }
        }

        final PropertyDefinition definition = Mockito.mock(PropertyDefinition.class);
        Mockito.when(jcrProperty.getDefinition()).thenReturn(definition);

        // if values is null, there was a property in the xml which is of type multiple but has no values
        Mockito.when(definition.isMultiple()).thenReturn(this.values == null || this.values.size() > 1);

        final UnsupportedOperationException unsupportedOperation = new UnsupportedOperationException("The method set value is not supported yet.");
        Mockito.doThrow(unsupportedOperation).when(jcrProperty).setValue(Matchers.anyString());
        Mockito.doThrow(unsupportedOperation).when(jcrProperty).setValue(Matchers.anyDouble());
        Mockito.doThrow(unsupportedOperation).when(jcrProperty).setValue(Matchers.anyBoolean());
        Mockito.doThrow(unsupportedOperation).when(jcrProperty).setValue(Matchers.anyLong());
        Mockito.doThrow(unsupportedOperation).when(jcrProperty).setValue(Matchers.any(Value.class));
        Mockito.doThrow(unsupportedOperation).when(jcrProperty).setValue(Matchers.any(Value[].class));
        Mockito.doThrow(unsupportedOperation).when(jcrProperty).setValue(Matchers.any(String[].class));

        return jcrProperty;
    }

    private void mockMultiValueProperty(final Property jcrProperty) throws RepositoryException {
        final List<Value> jcrValues = new ArrayList<Value>();
        for (String value : this.values) {
            jcrValues.add(mockValue(value));
        }
        final Value[] valuesArray = new Value[jcrValues.size()];
        Mockito.when(jcrProperty.getValues()).thenReturn(jcrValues.toArray(valuesArray));
    }

    private void mockSingleValueProperty(final Property jcrProperty) throws RepositoryException {
        final String stringValue = this.values.get(0);
        Mockito.when(jcrProperty.getString()).thenReturn(stringValue);

        if ("Date".equals(this.type)) {
            Mockito.when(jcrProperty.getDate()).thenReturn(ISO8601.parse(stringValue));
        } else if ("Boolean".equals(this.type)) {
            Mockito.when(jcrProperty.getBoolean()).thenReturn(Boolean.parseBoolean(stringValue));
        } else if ("Double".equals(this.type)) {
            Mockito.when(jcrProperty.getDouble()).thenReturn(Double.parseDouble(stringValue));
        } else if ("Long".equals(this.type)) {
            Mockito.when(jcrProperty.getLong()).thenReturn(Long.parseLong(stringValue));
        }

        final Value mockValue = mockValue(stringValue);
        Mockito.when(jcrProperty.getValue()).thenReturn(mockValue);
    }

    private void mockType(final Property jcrProperty) throws RepositoryException {
        if ("Date".equals(this.type)) {
            Mockito.when(jcrProperty.getType()).thenReturn(PropertyType.DATE);
        } else if ("Boolean".equals(this.type)) {
            Mockito.when(jcrProperty.getType()).thenReturn(PropertyType.BOOLEAN);
        } else if ("Double".equals(this.type)) {
            Mockito.when(jcrProperty.getType()).thenReturn(PropertyType.DOUBLE);
        } else if ("Long".equals(this.type)) {
            Mockito.when(jcrProperty.getType()).thenReturn(PropertyType.LONG);
        } else if ("String".equals(this.type)) {
            Mockito.when(jcrProperty.getType()).thenReturn(PropertyType.STRING);
        }
    }

    private Value mockValue(String stringValue) throws RepositoryException {
        Value value = Mockito.mock(Value.class);
        Mockito.when(value.getString()).thenReturn(stringValue);
        if ("Date".equals(this.type)) {
            Mockito.when(value.getDate()).thenReturn(ISO8601.parse(stringValue));
            Mockito.when(value.getType()).thenReturn(PropertyType.DATE);
        } else if ("Boolean".equals(this.type)) {
            Mockito.when(value.getBoolean()).thenReturn(Boolean.parseBoolean(stringValue));
            Mockito.when(value.getType()).thenReturn(PropertyType.BOOLEAN);
        } else if ("Double".equals(this.type)) {
            Mockito.when(value.getDouble()).thenReturn(Double.parseDouble(stringValue));
            Mockito.when(value.getType()).thenReturn(PropertyType.DOUBLE);
        } else if ("Long".equals(this.type)) {
            Mockito.when(value.getLong()).thenReturn(Long.parseLong(stringValue));
            Mockito.when(value.getType()).thenReturn(PropertyType.LONG);
        } else if ("String".equals(this.type) || "String[]".equals(this.type)) {
            Mockito.when(value.getType()).thenReturn(PropertyType.STRING);
        }

        return value;
    }

    private static class PathAnswer implements Answer<String> {
        private final MockProperty mockProperty;

        public PathAnswer(final MockProperty mockProperty) {
            this.mockProperty = mockProperty;
        }

        public String answer(final InvocationOnMock invocationOnMock) {
            return mockProperty.getPath();
        }
    }

    private static class ParentAnswer implements Answer<Item> {
        private final MockProperty mockProperty;

        public ParentAnswer(final MockProperty mockProperty) {
            this.mockProperty = mockProperty;
        }

        public Item answer(final InvocationOnMock invocationOnMock) throws RepositoryException {
            return mockProperty.getParent().getJcrMock();
        }
    }
}
