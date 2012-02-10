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

package org.onehippo.forge.utilities.hst.simpleocm.build;

import javax.jcr.Node;

import org.hippoecm.hst.content.beans.ContentNodeBinder;
import org.hippoecm.hst.content.beans.ContentNodeBindingException;

/**
 * Builds a node based on annotated beans.
 *
 * @version $Id: NodeBuilder.java 101644 2010-12-19 18:23:01Z jbloemendal $
 * @see org.onehippo.forge.utilities.hst.simpleocm.JcrPath
 * @see org.onehippo.forge.utilities.hst.simpleocm.JcrNodeType
 */
public interface NodeBuilder extends ContentNodeBinder {

    /**
     * Creates a node for an object and appends it to the parent node.
     *
     * @param parentNode       the parent node
     * @param name             the name of the new node
     * @param obj              the obj (annotated bean) which reflects the data for the node
     * @param sameNameSiblings if true it will create new child nodes with same names, otherwise returns the existing
     *                         child with that name
     * @return the new created node, or existing child node if sameNameSiblings is false
     * @throws ContentNodeBindingException if creating the node fails
     */
    Node createNode(Node parentNode, String name, Object obj, boolean sameNameSiblings) throws ContentNodeBindingException;

    /**
     * Builds the node for the passed object.
     * First creates a child node with the specified name for the object to populate the created node later with
     * all annotated field.
     * @see NodeBuilder#createNode(javax.jcr.Node, String, Object, boolean)
     * @see NodeBuilder#bind(Object, javax.jcr.Node)
     * @param parent parent node to add the child to
     * @param nodeName the name of the new node to create
     * @param object the object to marshal to JCR
     * @return the build node
     * @throws ContentNodeBindingException if building the node fails
     */
    Node build(final Node parent, final String nodeName, final Object object) throws ContentNodeBindingException;

}