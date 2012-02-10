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

import java.lang.reflect.Field;

import javax.jcr.Node;

import org.hippoecm.hst.content.beans.ContentNodeBindingException;

/**
 * Builds a property / child node for an field of an object.
 * @see org.onehippo.forge.utilities.hst.simpleocm.util.Converter
 * @see org.onehippo.forge.utilities.hst.simpleocm.util.ConverterAdapter
 */
public interface PropertyBuilder {

    /**
     * Builds a property / child node with the specified relative path of a field of the passed object.
     *
     * @param node    the node to set / append the property / child node
     * @param relPath the relative path for the new property / child node
     * @param field   the field to get the data / information from to build
     * @param obj     the object the field is bound to
     * @throws ContentNodeBindingException if building the property fails.
     */
    void buildProperty(Node node, String relPath, Field field, Object obj) throws ContentNodeBindingException;

}
