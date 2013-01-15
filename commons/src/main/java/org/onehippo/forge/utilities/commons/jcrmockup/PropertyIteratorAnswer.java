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

package org.onehippo.forge.utilities.commons.jcrmockup;

import java.util.Iterator;

import javax.jcr.Property;
import javax.jcr.PropertyIterator;
import javax.jcr.RepositoryException;

import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;


public class PropertyIteratorAnswer implements Answer<PropertyIterator> {

    private static final String REMOVE = "remove";
    private static final String NEXT = "next";
    private static final String NEXT_PROPERTY = "nextProperty";
    private MockNode mockNode;

    public PropertyIteratorAnswer(MockNode mockNode) {
        this.mockNode = mockNode;
    }

    private PropertyIterator createIterator(final Iterator<MockProperty> iterator) {
        final PropertyIterator propertyIterator = Mockito.mock(PropertyIterator.class);
        Mockito.when(propertyIterator.hasNext()).thenAnswer(new BooleanAnswer(iterator));

        final Answer<Property> propertyAnswer = new PropertyAnswer(iterator);
        Mockito.when(propertyIterator.nextProperty()).thenAnswer(propertyAnswer);
        Mockito.when(propertyIterator.next()).thenAnswer(propertyAnswer);
        Mockito.doAnswer(propertyAnswer).when(propertyIterator).remove();

        return propertyIterator;
    }

    public PropertyIterator answer(final InvocationOnMock invocationOnMock) {
        final Object args[] = invocationOnMock.getArguments();
        if (args.length == 0) {
            return createIterator(mockNode.getMockProperties().iterator());
        }
        throw new UnsupportedOperationException("The mock operation is not supported.");
    }

    private static class BooleanAnswer implements Answer<Boolean> {
        private final Iterator<MockProperty> iterator;

        public BooleanAnswer(final Iterator<MockProperty> iterator) {
            this.iterator = iterator;
        }

        public Boolean answer(final InvocationOnMock invocationOnMock) {
            return iterator.hasNext();
        }
    }

    private static class PropertyAnswer implements Answer<Property> {
        private MockProperty nextProperty;
        private final Iterator<MockProperty> iterator;

        public PropertyAnswer(final Iterator<MockProperty> iterator) {
            this.iterator = iterator;
            nextProperty = null;
        }

        public Property answer(final InvocationOnMock invocationOnMock) throws RepositoryException {
            final String methodName = invocationOnMock.getMethod().getName();
            if (REMOVE.equals(methodName)) {
                this.nextProperty.setRemoved(true);
            } else if (NEXT.equals(methodName) || NEXT_PROPERTY.equals(methodName)) {
                this.nextProperty = iterator.next();
                if (this.nextProperty != null) {
                    return this.nextProperty.mockJcrProperty();
                }
            }
            return null;
        }
    }
}
