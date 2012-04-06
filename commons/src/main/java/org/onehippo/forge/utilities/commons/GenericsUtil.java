/*
 * Copyright 2012 Hippo
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

package org.onehippo.forge.utilities.commons;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import javax.jcr.Node;
import javax.jcr.Property;
import javax.jcr.PropertyType;
import javax.jcr.RepositoryException;
import javax.jcr.Value;

import org.apache.jackrabbit.value.BooleanValue;
import org.apache.jackrabbit.value.DateValue;
import org.apache.jackrabbit.value.DoubleValue;
import org.apache.jackrabbit.value.LongValue;
import org.apache.jackrabbit.value.StringValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public final class GenericsUtil {

    private static final Logger logger = LoggerFactory.getLogger(GenericsUtil.class);

    /**
     * Private constructor preventing instantiation.
     */
    private GenericsUtil() {
    }

    @SuppressWarnings(value = "unchecked")
    public static <T> T getValue(final Value value, final Class<T> clazz) throws RepositoryException {
        final int valueType = value.getType();
        if (int.class.equals(clazz) || Integer.class.equals(clazz)) {
            throw new UnsupportedOperationException("JCR doesn't support integer values. Please use the type Long to get the value.");
        }
        switch (valueType) {
            case PropertyType.BOOLEAN:
                if (Boolean.class.equals(clazz) || boolean.class.equals(clazz)) {
                    return (T) (Boolean) value.getBoolean();
                }
                break;
            case PropertyType.DATE:
                if (Calendar.class.equals(clazz)) {
                    return (T) value.getDate();
                }
                break;
            case PropertyType.DOUBLE:
                if (Double.class.equals(clazz) || double.class.equals(clazz)) {
                    return (T) (Double) value.getDouble();
                }
                break;
            case PropertyType.LONG:
                if (Long.class.equals(clazz) || long.class.equals(clazz)) {
                    return (T) (Long) value.getLong();
                }
                break;
            case PropertyType.STRING:
                if (String.class.equals(clazz)) {
                    return (T) value.getString();
                }
                break;
        }
        throw new UnsupportedOperationException("The type "+clazz+" with value type '"+valueType+"' is not supported.");
    }

    @SuppressWarnings(value = "unchecked")
    public static <T> Collection<T> getPropertyValues(final Property property, final Class<? extends Collection> clazz, final Class<T> genericType) throws RepositoryException, IllegalAccessException, InstantiationException {
        if (!property.getDefinition().isMultiple()) {
            return Collections.emptyList();
        }
        final Collection<T> collection = clazz.newInstance();
        for (final Value value : property.getValues()) {
            collection.add(getValue(value, genericType));
        }
        return collection;
    }

    @SuppressWarnings(value = "unchecked")
    public static <T> Collection<T> getPropertyValues(final Node node, final String path, final Class<? extends Collection> clazz, final Class<T> genericType) throws RepositoryException, InstantiationException, IllegalAccessException {
        if (!node.hasProperty(path)) {
            return clazz.newInstance();
        }
        return getPropertyValues(node.getProperty(path), clazz, genericType);
    }

    public static <T> T getPropertyValue(final Property property, final Class<T> clazz) throws RepositoryException {
        if (property.getDefinition().isMultiple() || clazz.isArray()) {
            throw new UnsupportedOperationException("The method get property value can only be used for non array types. Use getPropertyValues() instead.");
        }
        return getValue(property.getValue(), clazz);
    }

    public static <T> T getPropertyValue(final Node node, final String path, final Class<T> clazz) throws RepositoryException {
        if (!node.hasProperty(path)) {
            return null;
        }
        return getPropertyValue(node.getProperty(path), clazz);
    }

    public static void setProperty(final Node node, final String relativePath, final Object object) throws RepositoryException {
        logger.debug("Setting property '{}' to value '{}'", new Object[] {node.getPath()+"/"+relativePath, object});
        if (!node.isCheckedOut()) {
            node.checkout();
        }
        if (object == null) {
            if (node.hasProperty(relativePath)) {
                node.getProperty(relativePath).remove();
            }
        } else if (object instanceof String) {
            node.setProperty(relativePath, (String) object);
        } else if (object instanceof String[]) {
            node.setProperty(relativePath, (String[]) object);
        } else if (object instanceof Boolean) {
            node.setProperty(relativePath, (Boolean) object);
        } else if (object instanceof Double) {
            node.setProperty(relativePath, (Double) object);
        } else if (object instanceof Integer) {
            node.setProperty(relativePath, (Integer) object);
        } else if (object instanceof Long) {
            node.setProperty(relativePath, (Long) object);
        } else if (object instanceof Calendar) {
            node.setProperty(relativePath, (Calendar)object);
        } else {
            throw new UnsupportedOperationException("The method setProperty for object " + object + " is not supported yet.");
        }
    }

    public static <T> void setPropertyValues(final Node node, final String relativePath, final Collection<T> values) throws RepositoryException {
        logger.debug("Setting property '{}' values from collection.", node.getPath()+"/"+relativePath);
        if (!node.isCheckedOut()) {
            node.checkout();
        }
        if (values == null) {
            if (node.hasProperty(relativePath)) {
                logger.debug("Remove property "+node.getPath()+"/"+relativePath);
                node.getProperty(relativePath).remove();
            }
        } else {
            ArrayList<Value> newValues = new ArrayList<Value>();
            for (T object : values) {
                newValues.add(createValue(object));
            }
            node.setProperty(relativePath, newValues.toArray(new Value[newValues.size()]));
        }
    }

    public static <T> void setValues(final Property property, final Collection<T> values) throws RepositoryException {
        final Value[] newValues = new Value[values.size()];
        int index = 0;
        for (T object : values) {
            newValues[index++] = createValue(object);
        }
        property.setValue(newValues);
    }

    public static Value createValue(Object object) {
        if (object instanceof String) {
            return new StringValue((String) object);
        } else if (object instanceof Long) {
            return new LongValue((Long) object);
        } else if (object instanceof Double) {
            return new DoubleValue((Double) object);
        } else if (object instanceof Calendar) {
            return new DateValue((Calendar) object);
        } else if (object instanceof Boolean) {
            return new BooleanValue((Boolean) object);
        }
        throw new UnsupportedOperationException("Create value doesn't support the type '" + object.getClass() + "'");
    }

    public static List<Class<?>> getActualTypeParameters(Field field) {
        final List<Class<?>> actualTypeParameters = new ArrayList<Class<?>>();
        final Type genericFieldType = field.getGenericType();
        if (genericFieldType instanceof ParameterizedType) {
            final ParameterizedType type = (ParameterizedType) genericFieldType;
            Type[] fieldArgTypes = type.getActualTypeArguments();
            for (Type fieldArgType : fieldArgTypes) {
                actualTypeParameters.add((Class) fieldArgType);
            }
        }
        return actualTypeParameters;
    }

    public static <T extends Annotation> T getGenericAnnotation(final Field field, final int parameterIndex, final Class<T> annotationClazz) {
        final List<Class<?>> actualTypeParameters = getActualTypeParameters(field);
        if (actualTypeParameters.size() <= parameterIndex) {
            return null;
        }
        return actualTypeParameters.get(parameterIndex).getAnnotation(annotationClazz);
    }

    public static <T> T getListItem(List<T> list, int index) {
        if (list.size() > index) {
            return list.get(index);
        }
        return null;
    }
}
