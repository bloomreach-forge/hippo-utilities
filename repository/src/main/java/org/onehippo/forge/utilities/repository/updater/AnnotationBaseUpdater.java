/*
 * Copyright 2012 Hippo
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
package org.onehippo.forge.utilities.repository.updater;

import java.lang.reflect.Method;

import javax.jcr.Node;
import javax.jcr.RepositoryException;

import org.apache.commons.lang.StringUtils;
import org.hippoecm.repository.ext.UpdaterContext;
import org.hippoecm.repository.ext.UpdaterItemVisitor;
import org.hippoecm.repository.ext.UpdaterModule;
import org.onehippo.forge.utilities.repository.updater.annotations.PathVisitor;
import org.onehippo.forge.utilities.repository.updater.annotations.QueryVisitor;
import org.onehippo.forge.utilities.repository.updater.annotations.Updater;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * AnnotationBaseUpdater
 * <P>AnnotationBaseUpdater supports {@link Updater}, {@link PathVisitor} and {@link QueryVisitor} for your convenience.
 * So you can easily implement your custom updater only by using annotations!</P>
 * <P>You may define {@link PathVisitor} or/and {@link QueryVisitor} in any methods
 * which should be public and have only one parameter of {@link javax.jcr.Node} type.</P>
 * <P>Here is a simple example:</P>
 * <PRE>
 * @Updater(name = "Updater-demosite-2.05.00",
 *         start = { "demosite-2.04.00" },
 *         end = { "demosite-2.05.00" })
 * public class Updater_2_05_00 extends AnnotationBaseUpdater {
 *
 *     @PathVisitor(
 *             paths={
 *                     "/hippo:configuration/hippo:initialize/hippo-namespaces-demosite-comment",
 *                     "/hippo:configuration/hippo:initialize/demosite-sitemap"
 *                 }
 *             )
 *     public void deleteInitializeNodes(Node node) throws RepositoryException { node.remove(); }
 *
 *     @PathVisitor(
 *             paths={
 *                     "/hippo:namespaces/demosite/comment",
 *                     "/hst:hst/hst:configurations/demosite/hst:sitemap"
 *                 }
 *             )
 *     public void deleteNamespaceConfigurationNodes(Node node) throws RepositoryException { node.remove(); }
 * }
 * </PRE>
 * <P>
 * In the example above, the updater class defines its name and start/end version tags.
 * So, the updater will run on the system having 'demosite-2.04.00' value in '/hippo:configuration/hippo:initialize' node.
 * After running the updater, the system will have a new tag version, 'demosite-2.05.00' value in the node.
 * The example updater implementation shown above defined two methods, both of which must be public and
 * have only one {@link javax.jcr.Node} type argument. The first method, #deleteInitializeNodes(Node),
 * is annotated by {@link PathVisitor}, so {@link AnnotationBaseUpdater} will automatically register visitors
 * by the paths and the visitors will invoke the #deleteInitializeNodes(Node) automatically.
 * In the example, it just removes the visiting nodes in order to re-initialize the initialize items.
 * Also, the #deleteNamespaceConfigurationNodes(Node) method defines {@link PathVisitor} annotation as well
 * in order to remove namespace nodes and configuration nodes.
 * Anyway, you can define any methods with {@link PathVisitor} or {@link QueryVisitor}.
 * AnnotationBaseUpdater will register visitors to invoke your methods automatically.
 * </P>
 *
 * @author Woonsan Ko
 * @version $Id$
 */
public class AnnotationBaseUpdater implements UpdaterModule {

    private static Logger log = LoggerFactory.getLogger(AnnotationBaseUpdater.class);

    private Updater updater;

    public void register(final UpdaterContext context) {

        updater = getClass().getAnnotation(Updater.class);

        if (updater == null) {
            log.warn("Stopped processing the updater, {}, wasn't annotated by {}.", getClass().getName(), Updater.class.getName());
            return;
        }

        String name = updater.name();

        if (StringUtils.isBlank(name)) {
            log.warn("The name of the updater is blank. Stop processing.");
        } else {
            log.debug("registering updater module name: '{}'", name);
            context.registerName(name);
            log.debug("registered updater module name: '{}'", name);
        }

        String[] start = updater.start();

        if (start != null) {
            for (String tag : start) {
                if (StringUtils.isBlank(tag)) {
                    log.warn("Invalid start tag: '{}'. Skipped", tag);
                    continue;
                }
                log.debug("registering updater start tag: '{}'", tag);
                context.registerStartTag(tag);
                log.debug("registered updater start tag: '{}'", tag);
            }
        }

        String[] end = updater.end();

        if (end != null) {
            for (String tag : end) {
                if (StringUtils.isBlank(tag)) {
                    log.warn("Invalid end tag: '{}'. Skipped", tag);
                    continue;
                }
                log.debug("registering updater end tag: '{}'", tag);
                context.registerEndTag(tag);
                log.debug("registered updater end tag: '{}'", tag);
            }
        }

        String[] before = updater.before();

        if (before != null) {
            for (String value : before) {
                if (StringUtils.isBlank(value)) {
                    log.warn("Invalid before value: '{}'. Skipped", value);
                    continue;
                }
                log.debug("registering updater before: '{}'", value);
                context.registerBefore(value);
                log.debug("registered updater before: '{}'", value);
            }
        }

        String[] after = updater.after();

        if (after != null) {
            for (String value : after) {
                if (StringUtils.isBlank(value)) {
                    log.warn("Invalid after value: '{}'. Skipped", value);
                    continue;
                }
                log.debug("registering updater after: '{}'", value);
                context.registerAfter(value);
                log.debug("registered updater after: '{}'", value);
            }
        }

        registerVisitorsByMethodAnnotations(context);
    }

    private void registerVisitorsByMethodAnnotations(final UpdaterContext context) {

        Method[] methods = getClass().getMethods();

        for (final Method method : methods) {

            PathVisitor pathVisitor = method.getAnnotation(PathVisitor.class);
            QueryVisitor queryVisitor = method.getAnnotation(QueryVisitor.class);

            if (pathVisitor == null && queryVisitor == null) {
                log.debug("Skipping the method, {}, which wasn't annotated by PathVisitor or QueryVisitor.", method);
                continue;
            }

            Class<?> [] paramTypes = method.getParameterTypes();

            if (paramTypes.length != 1 || !paramTypes[0].isAssignableFrom(Node.class)) {
                log.warn("Invalid visitor annotated method, which should have only one javax.jcr.Node parameter: {}", method);
                continue;
            }

            if (pathVisitor != null) {
                String [] paths = pathVisitor.paths();
                for (final String path : paths) {
                    log.debug("registering path visitor on '{}' to invoke '{}'", path, method);
                    context.registerVisitor(new UpdaterItemVisitor.PathVisitor(path) {
                        @Override
                        protected void leaving(Node node, int level) throws RepositoryException {
                            try {
                                method.invoke(AnnotationBaseUpdater.this, node);
                            } catch (Exception e) {
                                handleException(e, node, path);
                            }
                        }
                    });
                    log.debug("registered path visitor on '{}' to invoke '{}'", path, method);
                }
            }

            if (queryVisitor != null) {
                final String language = queryVisitor.language();
                String [] queries = queryVisitor.queries();
                for (final String query : queries) {
                    log.debug("registering query visitor on '{}' to invoke '{}'", query, method);
                    context.registerVisitor(new UpdaterItemVisitor.QueryVisitor(query, language) {
                        @Override
                        protected void leaving(Node node, int level) throws RepositoryException {
                            try {
                                method.invoke(AnnotationBaseUpdater.this, node);
                            } catch (Exception e) {
                                handleException(e, node, query);
                            }
                        }
                    });
                    log.debug("registered query visitor on '{}' to invoke '{}'", query, method);
                }
            }
        }
    }

    private void handleException(Exception ex, Node node, String name) {
        try {
            log.error("Caught exception for visitor " + name + " and node " + node.getPath(), ex);
        } catch (Exception e) {
            log.error("Caught exception in handleException() for visitor " + name, ex);
        }
    }

}
