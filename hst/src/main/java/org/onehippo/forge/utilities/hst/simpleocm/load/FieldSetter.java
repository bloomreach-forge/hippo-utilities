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

package org.onehippo.forge.utilities.hst.simpleocm.load;

import java.lang.reflect.Field;

import javax.jcr.Node;

import org.hippoecm.hst.content.beans.ContentNodeBindingException;

/**
 * Sets a value / object of a field based on a property / child node of a node.
 * @see org.onehippo.forge.utilities.hst.simpleocm.util.Converter
 * @see org.onehippo.forge.utilities.hst.simpleocm.util.ConverterAdapter
 */
public interface FieldSetter {

    /**
     * Sets the value of a field, by loading it from the property / child node specified by the relative path and a node.
     * @param obj the object which corresponds to the field
     * @param field the field to set
     * @param node the node which provides the data.
     * @param relativePath the relative path
     * @throws ContentNodeBindingException if setting the field fails
     */
    void setFieldValue(final Object obj, final Field field, final Node node, final String relativePath) throws ContentNodeBindingException;

}
