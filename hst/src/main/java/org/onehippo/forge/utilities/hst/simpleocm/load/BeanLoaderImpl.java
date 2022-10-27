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

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.Property;
import javax.jcr.RepositoryException;

import org.apache.commons.lang.StringUtils;
import org.hippoecm.hst.content.beans.ContentNodeBindingException;
import org.hippoecm.repository.api.NodeNameCodec;
import org.onehippo.forge.utilities.commons.GenericsUtil;
import org.onehippo.forge.utilities.hst.simpleocm.JcrNodeType;
import org.onehippo.forge.utilities.hst.simpleocm.JcrPath;
import org.onehippo.forge.utilities.hst.simpleocm.util.Converter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Populates a annotated bean from a repository.
 *
 * @see org.onehippo.forge.utilities.hst.simpleocm.JcrNodeType
 * @see org.onehippo.forge.utilities.hst.simpleocm.JcrPath
 * @version $Id: BeanLoaderImpl.java 103480 2011-01-19 21:27:01Z jbloemendal $
 */
public class BeanLoaderImpl implements BeanLoader, FieldSetter {

    private static final Logger logger = LoggerFactory.getLogger(BeanLoaderImpl.class);

    /**
     * @see FieldSetter
     * @see javax.jcr.Node#getNodes(String) for collections with path pattern
     */
    public void setFieldValue(final Object obj, final Field field, final Node node, final String relativePath) throws ContentNodeBindingException {
        final Class<?> fieldType = field.getType();
        try {
            if (!field.isAccessible()) {
                field.setAccessible(true);
            }
            if ("*".equals(relativePath)) {
                setAnyPathCompoundCollections(obj, field, node);
            } else if (List.class.equals(fieldType) && GenericsUtil.getGenericAnnotation(field, 0, JcrNodeType.class) != null) {
                setCompoundCollection(obj, field, node.getNodes(relativePath), ArrayList.class);  // NOSONAR (prevent warning about loose coupling; we need the ArrayList.class here)
            } else if (Set.class.equals(fieldType) && GenericsUtil.getGenericAnnotation(field, 0, JcrNodeType.class) != null) {
                setCompoundCollection(obj, field, node.getNodes(relativePath), HashSet.class);    // NOSONAR (prevent warning about loose coupling; we need the HashSet.class here)
            } else if (fieldType.getAnnotation(JcrNodeType.class) != null) {
                setCompound(obj, field, node, relativePath);
            } else {
                setPrimitive(obj, field, node, relativePath);
            }
        } catch (IllegalAccessException accessException) {
            throw new ContentNodeBindingException("Error setting the field '"+field.getName()+"' of type "+fieldType, accessException);
        } catch (RepositoryException repositoryException) {
            throw new ContentNodeBindingException("Error setting the field '"+field.getName()+"' of type "+fieldType+", relative jcr path '"+relativePath+"'", repositoryException);
        } catch (InstantiationException instantiationException) {
            throw new ContentNodeBindingException("Error setting the field '"+field.getName()+"' of type "+fieldType, instantiationException);
        }
    }

    /**
     * Sets an field from a primitive value, including multiple values
     *
     * @param obj the object which belongs to the field
     * @param field the field to set
     * @param node the corresponding node
     * @param relativePath the relative path from the node
     * @throws RepositoryException if setting the value fails
     * @throws IllegalAccessException if the field could not be accessed
     * @throws InstantiationException if instantiating the field object fails
     */
    private void setPrimitive(Object obj, Field field, Node node, String relativePath) throws RepositoryException, IllegalAccessException, InstantiationException {
        if (!node.hasProperty(relativePath)) {
            logger.debug("The node '{}' doesn't have a property '{}'", node.getPath(), relativePath);
            return;
        }
        final List<Class<?>> actualTypeParameters = GenericsUtil.getActualTypeParameters(field);
        final Property property = node.getProperty(relativePath);
        final Class<?> fieldType = field.getType();
        if (property.getDefinition().isMultiple() && actualTypeParameters.size() > 0) {
            if (List.class.equals(fieldType)) {
                logger.debug("Loading list of primitives for property '{}'", property.getPath());
                field.set(obj, GenericsUtil.getPropertyValues(property, ArrayList.class, actualTypeParameters.get(0))); // NOSONAR (prevent warning about loose coupling; we need the ArrayList.class here)
            } else if (Set.class.equals(fieldType)) {
                logger.debug("Loading set of primitives for property '{}'", property.getPath());
                field.set(obj, GenericsUtil.getPropertyValues(property, HashSet.class, actualTypeParameters.get(0))); // NOSONAR (prevent warning about loose coupling; we need the HashSet.class here)
            } else {
                throw new UnsupportedOperationException("For primitive collections only List and Set are allowed.");
            }
        } else {
            logger.debug("Loading primitive value for property '{}'", property.getPath());
            field.set(obj, GenericsUtil.getPropertyValue(property, fieldType));
        }
    }

    /**
     * Sets the a field value from a compound node
     *
     * @param obj the object the field belongs to
     * @param field the field to set
     * @param node the corresponding node
     * @param relativePath the relative path from the node
     * @throws IllegalAccessException if accessing the field fails
     * @throws InstantiationException if instantiating the object for the field fails
     * @throws RepositoryException
     * @throws ContentNodeBindingException if setting the field compound fails
     */
    private void setCompound(final Object obj, final Field field, final Node node, final String relativePath) throws IllegalAccessException, InstantiationException, RepositoryException, ContentNodeBindingException {
        if (!node.hasNode(relativePath)) {
            logger.debug("The node '{}' doesn't have a node '{}'", node.getPath(), relativePath);
            return;
        }
        final Class<?> fieldType = field.getType();
        final JcrNodeType nodeTypeAnnotation = fieldType.getAnnotation(JcrNodeType.class);
        if (nodeTypeAnnotation == null) {
            return;
        }
        Object fieldObject = field.get(obj);
        if (fieldObject == null) {
            fieldObject = fieldType.newInstance();
        }
        final Node childNode = node.getNode(relativePath);
        logger.debug("Loading object from node '{}'", childNode.getPath());
        loadBean(childNode, fieldObject);
        if (!BeanLoader.class.equals(nodeTypeAnnotation.loader())) {
            instantiateBeanLoader(nodeTypeAnnotation.loader()).loadBean(childNode, fieldObject);
        }
        field.set(obj, fieldObject);
    }

    /**
     * Sets a map or collection which has an generic type parameter annotated with JcrNodeType
     *
     * @param obj the object the field belongs to
     * @param field the field to set
     * @param node the corresponding node, which has child nodes of the type specified by JcrType
     * @throws IllegalAccessException if accessing the field fails
     * @throws InstantiationException if instantiating the field object fails
     * @throws RepositoryException
     * @throws ContentNodeBindingException if setting the collections fails
     */
    private void setAnyPathCompoundCollections(final Object obj, final Field field, final Node node) throws IllegalAccessException, InstantiationException, RepositoryException, ContentNodeBindingException {
        final Class<?> fieldType = field.getType();
        if (!fieldType.isInterface()) {
           throw new UnsupportedOperationException("The type " + fieldType + " is not supported.");
        }
        logger.debug("Loading any path compound nodes.");
        if (Map.class.equals(field.getType())) {
            setMap(obj, field, node.getNodes());
        } else if (List.class.equals(field.getType())) {
            setCompoundCollection(obj, field, node.getNodes(), ArrayList.class); // NOSONAR (prevent warning about loose coupling; we need the ArrayList.class here)
        } else if (Set.class.equals(field.getType())) {
            setCompoundCollection(obj, field, node.getNodes(), HashSet.class); // NOSONAR (prevent warning about loose coupling; we need the HashSet.class here)
        } else {
            throw new UnsupportedOperationException("The type " + fieldType + " is not supported.");
        }
    }

    /**
     * Sets a collection which has an generic type parameter annotated with JcrNodeType
     * @param obj the object the field belongs to
     * @param field the field to set
     * @param nodeIterator the nodes which are loaded in the collection
     * @param instantiateClass the class which will be used to instantiate the collection e.g. List.class, Set.class ...
     * @throws IllegalAccessException if accessing the field fails
     * @throws RepositoryException
     * @throws InstantiationException if instanciating the object fails
     * @throws ContentNodeBindingException if setting the collection fails
     */
    @SuppressWarnings(value = "unchecked")
    private void setCompoundCollection(final Object obj, final Field field, final NodeIterator nodeIterator, final Class<? extends Collection> instantiateClass) throws IllegalAccessException, RepositoryException, InstantiationException, ContentNodeBindingException {
        Object fieldObject = field.get(obj);
        final List<Class<?>> actualTypeParameters = GenericsUtil.getActualTypeParameters(field);
        if (actualTypeParameters.size() != 1) {
            return;
        }
        if (fieldObject == null) {
            fieldObject = instantiateClass.newInstance();
        }
        final Class<?> valueType = actualTypeParameters.get(0);
        final JcrNodeType nodeTypeAnnotation = valueType.getAnnotation(JcrNodeType.class);
        if (nodeTypeAnnotation == null) {
            return;
        }
        logger.debug("Loading collection objects");
        while (nodeIterator.hasNext()) {
            final Node childNode = nodeIterator.nextNode();
            if (!childNode.isNodeType(nodeTypeAnnotation.value())) {
                continue;
            }
            logger.debug("Loading object from node '{}'", childNode.getPath());
            final Object childObject = valueType.newInstance();
            loadBean(childNode, childObject);
            if (!BeanLoader.class.equals(nodeTypeAnnotation.loader())) {
                instantiateBeanLoader(nodeTypeAnnotation.loader()).loadBean(childNode, childObject);
            }
            logger.debug("Adding object to collection '{}'", childObject);
            ((Collection) fieldObject).add(childObject);
        }
        if (((Collection) fieldObject).size() == 0) {
            logger.debug("Empty collection instantiated, no child nodes of type '{}' found", nodeTypeAnnotation.value());
        }
        field.set(obj, fieldObject);
    }

    /**
     * Sets a map which has an generic second type parameter annotated with JcrNodeType.
     * The map entries are [decodedNodeName,Object]
     *
     * @param obj the object the field belongs to
     * @param field the field to set
     * @param nodeIterator a node iterator with nodes, that should be populated in the map
     * @throws IllegalAccessException if accessing the field fails
     * @throws InstantiationException if instantiating the field object fails
     * @throws RepositoryException
     * @throws ContentNodeBindingException if setting the field fails
     */
    @SuppressWarnings(value = "unchecked")
    private void setMap(final Object obj, final Field field, final NodeIterator nodeIterator) throws IllegalAccessException, InstantiationException, RepositoryException, ContentNodeBindingException {
        final List<Class<?>> actualTypeParameters = GenericsUtil.getActualTypeParameters(field);
        if (!String.class.equals(actualTypeParameters.get(0))) {
            throw new UnsupportedOperationException("The key type parameter '" + actualTypeParameters.get(0) + "' is not supported. Only Strings are allowed");
        }
        Object fieldObject = field.get(obj);
        if (fieldObject == null) {
            fieldObject = new HashMap();
        }
        final Class<?> valueType = actualTypeParameters.get(1);
        final JcrNodeType nodeTypeAnnotation = valueType.getAnnotation(JcrNodeType.class);
        if (nodeTypeAnnotation == null) {
            return;
        }
        logger.debug("Loading map entries");
        while (nodeIterator.hasNext()) {
            final Node childNode = nodeIterator.nextNode();
            if (!childNode.isNodeType(nodeTypeAnnotation.value())) {
                continue;
            }
            logger.debug("Loading map entry from node '{}'", childNode.getPath());
            final Object childObject = valueType.newInstance();
            loadBean(childNode, childObject);
            if (!BeanLoader.class.equals(nodeTypeAnnotation.loader())) {
                instantiateBeanLoader(nodeTypeAnnotation.loader()).loadBean(childNode, childObject);
            }
            final String decodedChildNodeName = NodeNameCodec.decode(childNode.getName());
            logger.debug("Adding map entry ['{}','{}']", decodedChildNodeName, childObject);
            ((Map) fieldObject).put(decodedChildNodeName, childObject);
        }
        if (((Map) fieldObject).size() == 0) {
            logger.debug("Empty map instantiated, no nodes of type '{}' found.", nodeTypeAnnotation.value());
        }
        field.set(obj, fieldObject);
    }

    /**
     * Populate the annotated bean from a node
     *
     * @param node the node which holds the data to populate the bean
     * @param bean the bean to load from the node
     * @throws ContentNodeBindingException
     */
    public void loadBean(final Node node, final Object bean) throws ContentNodeBindingException {
        Class<?> clazz = bean.getClass();
        try {
            while (!Object.class.equals(clazz)) {
                loadFieldsForClass(node, bean, clazz);
                final Class<?> superClazz = clazz.getSuperclass();
                JcrNodeType nodeTypeAnnotation = superClazz.getAnnotation(JcrNodeType.class);
                if (nodeTypeAnnotation != null && !BeanLoader.class.equals(nodeTypeAnnotation.loader())) {
                    instantiateBeanLoader(nodeTypeAnnotation.loader()).loadBean(node, bean);
                }
                clazz = clazz.getSuperclass();
            }
        } catch (IllegalAccessException accessException) {
            throw new ContentNodeBindingException("Error loading bean '" + bean + "'", accessException);
        } catch (InstantiationException instantiationException) {
            throw new ContentNodeBindingException("Error loading bean '" + bean + "'", instantiationException);
        } catch (RepositoryException repositoryException) {
            throw new ContentNodeBindingException("Error loading bean '" + bean + "'", repositoryException);
        }
    }

    /**
     * Loads all annotated field of a class.
     *
     * @param node the node to load the values from
     * @param bean the bean to populate
     * @param clazz the class that specifies the fields to load
     * @throws RepositoryException if loading the fields fails
     * @throws InstantiationException if instantiating an object for a field fails
     * @throws IllegalAccessException if accessing a field fails
     * @throws ContentNodeBindingException if setting / loading the field falue fails
     */
    private void loadFieldsForClass(final Node node, final Object bean, final Class<?> clazz) throws RepositoryException, InstantiationException, IllegalAccessException, ContentNodeBindingException {
        logger.debug("Loading fields for class '{}' from node '{}'", clazz, node.getPath());
        for (Field field : clazz.getDeclaredFields()) {
            final Annotation annotations[] = field.getAnnotations();
            for (Annotation annotation : annotations) {
                if (!(annotation instanceof JcrPath)) {
                    continue;
                }
                String relativePath = ((JcrPath) annotation).value();
                if (StringUtils.isBlank(relativePath)) {
                    relativePath = field.getName();
                }
                final Class<? extends Converter> converterClass = ((JcrPath) annotation).converter();
                if (!Converter.class.equals(converterClass)) {
                    logger.debug("Load field '{}' with custom converter '{}' from '{}'", new Object[] {field.getName(), converterClass, node.getPath()+"/"+relativePath });
                    converterClass.newInstance().setFieldValue(bean, field, node, relativePath);
                } else {
                    logger.debug("Load field '{}' from '{}' ", field.getName(), node.getPath()+"/"+relativePath);
                    setFieldValue(bean, field, node, relativePath);
                }
            }
        }
    }

    /**
     * Instantiates a bean loader.
     *
     * @param beanLoaderClass the class of the loader
     * @return the instantiated bean loader
     * @throws ContentNodeBindingException if instantiating the loader fails
     */
    private BeanLoader instantiateBeanLoader(final Class<? extends BeanLoader> beanLoaderClass) throws ContentNodeBindingException {
        if (this.getClass().equals(beanLoaderClass)) {
            return this;
        }
        try {
            return beanLoaderClass.newInstance();
        } catch (IllegalAccessException accessException) {
            throw new ContentNodeBindingException("Error instantiating the bean loader "+beanLoaderClass, accessException);
        } catch (InstantiationException instantiationException) {
            throw new ContentNodeBindingException("Error instantiating the bean loader ", instantiationException);
        }
    }

}
