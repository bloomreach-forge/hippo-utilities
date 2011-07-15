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

package org.onehippo.forge.utilities.simpleocm.build;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.RepositoryException;

import org.apache.commons.lang.StringUtils;
import org.hippoecm.hst.content.beans.ContentNodeBinder;
import org.hippoecm.hst.content.beans.ContentNodeBindingException;
import org.hippoecm.repository.api.NodeNameCodec;
import org.onehippo.forge.utilities.simpleocm.JcrNodeType;
import org.onehippo.forge.utilities.simpleocm.JcrPath;
import org.onehippo.forge.utilities.simpleocm.util.Converter;
import org.onehippo.forge.utilities.simpleocm.util.GenericsUtil;
import org.onehippo.forge.utilities.simpleocm.build.NodeBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A node builder, to build a node in the repository from an annotated bean.
 */
public class NodeBuilderImpl implements NodeBuilder, PropertyBuilder {

    private static final Logger logger = LoggerFactory.getLogger(NodeBuilderImpl.class);

    /**
     * Builds a property by routing to helper method
     *
     * @param node                 the node to build the property / child node
     * @param relPath              the relative path for the new property / child node
     * @param value                the value to build the property / child node of
     * @param type                 the type of the the field
     * @param actualTypeParameters the type parameters of the field type
     * @throws RepositoryException         if building fails
     * @throws ContentNodeBindingException if building fails
     */
    @SuppressWarnings("unchecked")
    private void buildProperty(final Node node, final String relPath, final Object value, final Class<?> type, final List<Class<?>> actualTypeParameters) throws RepositoryException, ContentNodeBindingException {
        final JcrNodeType nodeTypeAnnotation = type.getAnnotation(JcrNodeType.class);
        Class<?> firstTypeParameter = GenericsUtil.getListItem(actualTypeParameters, 0);
        if (nodeTypeAnnotation != null) {
            build(node, relPath, value, nodeTypeAnnotation.sameNameSiblings());
        } else if ("*".equals(relPath) && Map.class.equals(type)) {
            buildChildNodesFromMap(node, (Map) value, actualTypeParameters, firstTypeParameter);
        } else if ((Set.class.equals(type) || List.class.equals(type)) && firstTypeParameter != null && firstTypeParameter.getAnnotation(JcrNodeType.class) != null) {
            buildChildNodesFromCollection(node, relPath, (Collection) value);
        } else if (Set.class.equals(type) || List.class.equals(type)) {
            GenericsUtil.setPropertyValues(node, relPath, (Collection) value);
        } else {
            GenericsUtil.setProperty(node, relPath, value);
        }
    }

    /**
     * Builds the child nodes from a collection, removes all existing child nodes with that name. An update behavior is
     * with a collection not possible, because it's not possible to make associations between nodes and collection
     * members. So the annotation value sameNameSiblings in JcrNodeType is ignored.
     *
     * @param parent     the parent node to append the new child nodes to
     * @param relPath    the relative path of the new child nodes (sameNameSiblings should be true @see
     *                   rg.onehippo.forge.psutils.simpleocm.JcrNodeType)
     * @param collection the collection
     * @throws ContentNodeBindingException   if building the child nodes fails
     * @throws javax.jcr.RepositoryException if removing old child nodes with fails
     */
    private void buildChildNodesFromCollection(final Node parent, final String relPath, final Collection collection) throws ContentNodeBindingException, RepositoryException {
        while (parent.hasNode(relPath)) {
            final Node childNode = parent.getNode(relPath);
            logger.debug("Removing existing child node '{}'", childNode.getPath());
            childNode.remove();
        }
        if (collection == null) {
            return;
        }
        logger.debug("Building child nodes from collection");
        for (Object object : collection) {
            build(parent, relPath, object, true);
        }
    }

    /**
     * Builds child nodes from a map, the key is providing the path for the child node
     *
     * @param parent               the parent node to add the child nodes to
     * @param map                  the map to build child nodes of
     * @param actualTypeParameters the generic type parameters of the map
     * @param firstTypeParameter   the first type parameter
     * @throws ContentNodeBindingException   if building the child nodes fails
     * @throws javax.jcr.RepositoryException if removing a existing child node with clashing name - key fails
     */
    private void buildChildNodesFromMap(final Node parent, final Map map, final List<Class<?>> actualTypeParameters, final Class<?> firstTypeParameter) throws ContentNodeBindingException, RepositoryException {
        if (actualTypeParameters.size() != 2 || !String.class.equals(firstTypeParameter)) {
            throw new UnsupportedOperationException("The path * is only supported for building Map<String, Object@JcrNodeType>.");
        }
        final JcrNodeType typeParameterAnnotation = actualTypeParameters.get(1).getAnnotation(JcrNodeType.class);
        if (typeParameterAnnotation == null) {
            throw new UnsupportedOperationException("The path * is only supported for building Map<String, Object@JcrNodeType>.");
        }

        removeMissingChildren(parent, map, typeParameterAnnotation);

        if (map == null) {
            return;
        }
        logger.debug("Building child nodes from map");
        for (Object object : map.entrySet()) {
            if (object instanceof Map.Entry) {
                final Map.Entry entry = (Map.Entry)object;
                build(parent, String.valueOf(entry.getKey()), entry.getValue(), typeParameterAnnotation.sameNameSiblings());
            }
        }
    }

    private void removeMissingChildren(final Node parent, final Map map, final JcrNodeType typeParameterAnnotation) throws RepositoryException {
        final NodeIterator childNodeIterator = parent.getNodes();
        while (childNodeIterator.hasNext()) {
            final Node childNode = childNodeIterator.nextNode();
            final String decodedChildNodeName = NodeNameCodec.decode(childNode.getName());
            if (!typeParameterAnnotation.value().equals(childNode.getPrimaryNodeType().getName())) {
                continue;
            }
            if (map == null) {
                if (logger.isDebugEnabled()) {
                    logger.debug("Map is null removing '{}' of type {}", childNode.getPath(), typeParameterAnnotation.value());
                }
                childNodeIterator.remove();
            } else if (!map.containsKey(decodedChildNodeName)) {
                if (logger.isDebugEnabled()) {
                    logger.debug("Map doesn't contain child node of name {}, removing '{}'", decodedChildNodeName, childNode.getPath());
                }
                childNodeIterator.remove();
            }
        }
    }

    /**
     * Build property
     *
     * @param node    the node to set / append the property / child node
     * @param relPath the relative path for the new property / child node
     * @param field   the field to get the data / information from to build
     * @param obj     the object the field is bound to
     * @throws ContentNodeBindingException
     */
    public void buildProperty(Node node, String relPath, Field field, Object obj) throws ContentNodeBindingException {
        try {
            if (!field.isAccessible()) {
                field.setAccessible(true);
            }
            final List<Class<?>> actualTypeParameters = GenericsUtil.getActualTypeParameters(field);
            final Object value = field.get(obj);
            final Class<?> type = field.getType();
            buildProperty(node, relPath, value, type, actualTypeParameters);
        } catch (IllegalAccessException accessException) {
            throw new ContentNodeBindingException("Error building property for object '" + obj.getClass() + "' relative path '" + relPath + "'", accessException);
        } catch (RepositoryException repositoryException) {
            throw new ContentNodeBindingException("Error building property for object '" + obj.getClass() + "' relative path '" + relPath + "'", repositoryException);
        }
    }

    /**
     * @see NodeBuilder
     */
    public Node createNode(final Node parent, final String name, final Object obj, final boolean sameNameSiblings) throws ContentNodeBindingException {
        final JcrNodeType annotation = obj.getClass().getAnnotation(JcrNodeType.class);
        if (annotation == null) {
            return null;
        }
        final String nodeName = NodeNameCodec.encode(name);
        try {
            if (!parent.isCheckedOut()) {
                parent.checkout();
            }
            if (!sameNameSiblings && parent.hasNode(nodeName)) {
                return parent.getNode(nodeName);
            } else {
                return parent.addNode(nodeName, annotation.value());
            }
        } catch (RepositoryException repositoryException) {
            throw new ContentNodeBindingException("Error creating node '" + name + "' for class " + obj.getClass(), repositoryException);
        }
    }

    /**
     * Bind all @see org.onehippo.forge.psutils.simpleocm.JcrPath annotated fields to the passed node. Also takes care of
     * inheritance and loops up, to check the fields of the super classes.
     * The session, or node needs to be saved to persist the changes.
     *
     * @param node   the node to populate
     * @param object the @see org.onehippo.forge.psutils.simpleocm.JcrNodeType annotated object
     * @throws org.hippoecm.hst.content.beans.ContentNodeBindingException
     *          also @see org.hippoecm.hst.content.beans.ContentNodeBinder#bind
     */
    public boolean bind(Object object, Node node) throws ContentNodeBindingException {
        Class<?> clazz = object.getClass();
        while (!Object.class.equals(clazz)) {
            buildPropertiesForClass(node, object, clazz);
            JcrNodeType nodeTypeAnnotation = clazz.getAnnotation(JcrNodeType.class);
            if (nodeTypeAnnotation != null && !ContentNodeBinder.class.equals(nodeTypeAnnotation.binder())) {
                  instantiateBinder(nodeTypeAnnotation.binder()).bind(object, node);
            }
            clazz = clazz.getSuperclass();
        }
        return true;
    }

    /**
     * Builds the properties and child nodes for a class.
     *
     * @param node   the node to populate
     * @param object the annotated object
     * @param clazz  the class, to get the annotations from
     * @throws org.hippoecm.hst.content.beans.ContentNodeBindingException
     *          if building a property / child node fails
     */
    private void buildPropertiesForClass(final Node node, final Object object, final Class<?> clazz) throws ContentNodeBindingException {
        logger.debug("Building properties for class '{}'", clazz);
        final Field fields[] = clazz.getDeclaredFields();
        for (Field field : fields) {
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
                    try {
                        logger.debug("Building property '{}' for field '{}' with converter '{}'", new Object[]{relativePath, field.getName(), converterClass});
                        converterClass.newInstance().buildProperty(node, relativePath, field, object);
                    } catch (IllegalAccessException accessException) {
                        throw new ContentNodeBindingException("Error building the property '" + relativePath + "' for class " + clazz, accessException);
                    } catch (InstantiationException instantiationException) {
                        throw new ContentNodeBindingException("Error building the property '" + relativePath + "' for class " + clazz, instantiationException);
                    }
                } else {
                    logger.debug("Building property '{}' for field '{}'", relativePath, field.getName());
                    buildProperty(node, relativePath, field, object);
                }
            }
        }
    }

    /**
     * Builds the node with all annotated properties and child nodes.
     * The session, or node needs to be saved to persist the changes.
     * @param parent   parent node to add the child to
     * @param nodeName the name of the new node to create
     * @param object   the object to marshal to JCR
     * @return a new {@link Node} or {@literal null} if the nodetype cannot be annotated
     * @throws ContentNodeBindingException
     */
    public Node build(final Node parent, final String nodeName, final Object object) throws ContentNodeBindingException {
        final Class<?> clazz = object.getClass();
        final JcrNodeType nodeTypeAnnotation = clazz.getAnnotation(JcrNodeType.class);
        if (nodeTypeAnnotation == null) {
            return null;
        }
        try {
            return build(parent, nodeName, object, nodeTypeAnnotation.sameNameSiblings());
        } catch (RepositoryException repositoryException) {
            throw new ContentNodeBindingException("Error building node '"+nodeName+"' from object "+object, repositoryException);
        }
    }

    /**
     * Builds the node with all annotated properties and child nodes. Local helper to use the sameNameSiblings behavior
     * internally as needed.
     *
     * @param parent   the parent node
     * @param nodeName the name of the new node (appended to the parent)
     * @param object   the annotated bean, which will be stored in the repository
     * @param sameNameSiblings if true will not create the child node, but instead take the existing node with the passed name
     * @return the build bean
     * @throws RepositoryException if building the node fails
     * @throws org.hippoecm.hst.content.beans.ContentNodeBindingException if building the node fails
     */
    private Node build(final Node parent, final String nodeName, final Object object, final boolean sameNameSiblings) throws ContentNodeBindingException, RepositoryException {
        if (object == null) {
            if (parent.hasNode(nodeName)) {
                parent.getNode(nodeName).remove();
            }
            return null;
        }
        final Class<?> clazz = object.getClass();
        final JcrNodeType nodeTypeAnnotation = clazz.getAnnotation(JcrNodeType.class);
        if (nodeTypeAnnotation == null) {
            return null;
        }
        logger.debug("Building child node '{}' for node '{}' from object '{}'", new Object[]{nodeName, parent.getPath(), object});
        final Node node = createNode(parent, nodeName, object, sameNameSiblings);
        if (node == null) {
            return null;
        }
        bind(object, node);
        if (!ContentNodeBinder.class.equals(nodeTypeAnnotation.binder())) {
            final ContentNodeBinder nodeBinder = instantiateBinder(nodeTypeAnnotation.binder());
            nodeBinder.bind(object, node);
        }
        return node;
    }

    /**
     * Instantiates a builder
     *
     * @param nodeBinder the class of the node builder to initialize
     * @return this if clazz equals this.getClass() otherwise the new instantiated node builder
     * @throws org.hippoecm.hst.content.beans.ContentNodeBindingException
     *          if instantiating the builder fails
     */
    private ContentNodeBinder instantiateBinder(Class<? extends ContentNodeBinder> nodeBinder) throws ContentNodeBindingException {
        if (this.getClass().equals(nodeBinder)) {
            return this;
        }
        try {
            return nodeBinder.newInstance();
        } catch (IllegalAccessException accessException) {
            throw new ContentNodeBindingException("Error occurred instantiating the node builder " + nodeBinder, accessException);
        } catch (InstantiationException instantiationException) {
            throw new ContentNodeBindingException("Error occurred instantiating the node builder " + nodeBinder, instantiationException);
        }
    }

}