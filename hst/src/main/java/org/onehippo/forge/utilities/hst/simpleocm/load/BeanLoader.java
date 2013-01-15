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

package org.onehippo.forge.utilities.hst.simpleocm.load;

import javax.jcr.Node;

import org.hippoecm.hst.content.beans.ContentNodeBindingException;

/**
 * Loads / populate an annotated bean from a node.
 */
public interface BeanLoader {

    /**
     * Load a annotated bean from the passed node.
     *
     * @param node the node which holds the data to populate the bean
     * @param bean the bean to load from the node
     * @throws ContentNodeBindingException if loading / populating the bean fails
     */
    void loadBean(Node node, Object bean) throws ContentNodeBindingException;

}
