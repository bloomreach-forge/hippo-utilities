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

package org.onehippo.forge.utilities.hst.simpleocm.model;

import javax.jcr.Node;
import javax.jcr.RepositoryException;

import org.onehippo.forge.utilities.hst.simpleocm.util.ConverterAdapter;
import org.onehippo.forge.utilities.hst.simpleocm.util.FieldWrapper;


public class StringObscurerConverter extends ConverterAdapter {
    @Override
    public void setField(final Node node, final String relativePath, final FieldWrapper fieldWrapper) throws RepositoryException, IllegalAccessException {
        if (!node.hasProperty(relativePath)) {
            return;
        }
        final String value = node.getProperty(relativePath).getString();
        final StringBuilder decoded = new StringBuilder();
        for (int index=0; index<value.length(); index++) {
            char c = value.charAt(index);
            decoded.append(--c);
        }
        fieldWrapper.setValue(decoded.toString());
    }

    @Override
    public void buildProperty(final FieldWrapper field, final Node node, final String relativePath) throws RepositoryException, IllegalAccessException {
        final String value = (String)field.getValue();
        if (value == null) {
            return;
        }
        final StringBuilder encoded = new StringBuilder();
        for (int index=0; index<value.length(); index++) {
            char c = value.charAt(index);
            encoded.append(++c);
        }
        node.setProperty(relativePath, encoded.toString());
    }
}
