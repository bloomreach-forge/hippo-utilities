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

package org.onehippo.forge.utilities.hst.simpleocm.util;

import java.lang.reflect.Field;

import javax.jcr.Node;
import javax.jcr.RepositoryException;

import org.hippoecm.hst.content.beans.ContentNodeBindingException;

/**
 * A simple adapter for a converter.
 * Takes care of field access, handles basic exceptions, and wraps a Field for simpler code.
 */
public abstract class ConverterAdapter implements Converter {

    public void setFieldValue(final Object obj, final Field field, final Node node, final String relativePath) throws ContentNodeBindingException {
        final FieldWrapper fieldWrapper = new FieldWrapper(obj, field);
        try {
            setField(node, relativePath, fieldWrapper);
        } catch (IllegalAccessException illegalAccess) {
            throw new ContentNodeBindingException("Error setting value to field '" + field.getName() + "' of class '" + obj.getClass() + "', relative node path '" + relativePath + "'", illegalAccess);
        } catch (RepositoryException repositoryException) {
            throw new ContentNodeBindingException("Error setting value to field '" + field.getName() + "' of class '" + obj.getClass() + "', relative node path '" + relativePath + "'", repositoryException);
        }
    }

    public void buildProperty(final Node node, final String relPath, final Field field, final Object obj) throws ContentNodeBindingException {
        final FieldWrapper fieldWrapper = new FieldWrapper(obj, field);
        try {
            buildProperty(fieldWrapper, node, relPath);
        } catch (IllegalAccessException accessException) {
            throw new ContentNodeBindingException("Error building property '" + relPath + "' for class " + obj.getClass(), accessException);
        } catch (RepositoryException repositoryException) {
            throw new ContentNodeBindingException("Error building property '" + relPath + "' for class " + obj.getClass(), repositoryException);
        }
    }

    public abstract void setField(Node node, String relativePath, FieldWrapper fieldWrapper) throws RepositoryException, IllegalAccessException;

    public abstract void buildProperty(FieldWrapper field, Node node, String relativePath) throws RepositoryException, IllegalAccessException;

}
