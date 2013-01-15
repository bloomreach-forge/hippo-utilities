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
import java.util.List;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.RepositoryException;

import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;


public class NodeIteratorAnswer implements Answer<NodeIterator> {

    private MockNode mockNode;

    public NodeIteratorAnswer(MockNode mockNode) {
        this.mockNode = mockNode;
    }

    public NodeIterator createIterator(List<MockNode> nodes) throws RepositoryException {
        final Iterator<MockNode> mockNodeIterator = nodes.iterator();
        final NodeIterator nodeIterator = Mockito.mock(NodeIterator.class);
        final Answer<Boolean> hasNextAnswer = new HasNextAnswer(mockNodeIterator);
        final Answer<Node> nextAnswer = new NextAnswer(mockNodeIterator);

        Mockito.when(nodeIterator.hasNext()).thenAnswer(hasNextAnswer);
        Mockito.when(nodeIterator.next()).thenAnswer(nextAnswer);
        Mockito.when(nodeIterator.nextNode()).thenAnswer(nextAnswer);
        Mockito.doAnswer(nextAnswer).when(nodeIterator).remove();
        Mockito.when(nodeIterator.getSize()).thenReturn((long) nodes.size());

        return nodeIterator;
    }

    public NodeIterator answer(InvocationOnMock invocation) throws RepositoryException {
        final Object args[] = invocation.getArguments();
        if (args.length == 1 && args[0] instanceof String) {
            return createIterator(mockNode.getMockChildNodesByName((String) args[0]));
        } else if (args.length == 0) {
            return createIterator(mockNode.getMockChildNodes());
        }
        return null;
    }

    private static class HasNextAnswer implements Answer<Boolean> {
        private final Iterator<MockNode> mockNodeIterator;

        public HasNextAnswer(final Iterator<MockNode> mockNodeIterator) {
            this.mockNodeIterator = mockNodeIterator;
        }

        public Boolean answer(InvocationOnMock invocationOnMock) {
            return mockNodeIterator.hasNext();
        }
    }

    private static class NextAnswer implements Answer<Node> {
        private MockNode currentNode;
        private final Iterator<MockNode> mockNodeIterator;

        public NextAnswer(final Iterator<MockNode> mockNodeIterator) {
            this.mockNodeIterator = mockNodeIterator;
        }

        public Node answer(InvocationOnMock invocationOnMock) throws RepositoryException {
            final String methodName = invocationOnMock.getMethod().getName();
            if ("remove".equals(methodName)) {
                mockNodeIterator.remove();
                if (this.currentNode != null) {
                    this.currentNode.setRemoved(true);
                }
                return null;
            } else if ("next".equals(methodName) || "nextNode".equals(methodName)) {
                this.currentNode = mockNodeIterator.next();
                return this.currentNode.getJcrMock();
            }
            return null;
        }
    }
}
