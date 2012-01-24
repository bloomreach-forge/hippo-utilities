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

package org.onehippo.forge.utilities.content.updater.support;

import java.lang.reflect.Method;

import javax.jcr.Node;
import javax.jcr.RepositoryException;

import org.apache.commons.lang.StringUtils;
import org.hippoecm.repository.ext.UpdaterContext;
import org.hippoecm.repository.ext.UpdaterItemVisitor;
import org.onehippo.forge.utilities.content.updater.annotations.PathVisitor;
import org.onehippo.forge.utilities.content.updater.annotations.QueryVisitor;
import org.onehippo.forge.utilities.content.updater.annotations.Updater;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AnnotationBaseUpdater extends BaseUpdater {

    private static Logger log = LoggerFactory.getLogger(AnnotationBaseUpdater.class);

    private Updater updater;

    public void register(final UpdaterContext context) {
        updater = getClass().getAnnotation(Updater.class);

        if (updater == null) {
            log.warn("The updater, {}, wasn't annotated by {}", getClass().getName(), Updater.class.getName());
        } else {
            String name = updater.name();
            if (StringUtils.isBlank(name)) {
                log.warn("The name of the updater is blank. Stop processing.");
            } else {
                log.debug("registering updater module name: '{}'", name);
                context.registerName(name);
                log.debug("registered updater module name: '{}'", name);
            }
        }

        super.register(context);

        if (updater == null) {
            log.warn("The updater, {}, wasn't annotated by {}", getClass().getName(), Updater.class.getName());
            return;
        }

        registerBeforeAfter(context);
        registerVisitorsByMethodAnnotations(context);
    }

    @Override
    protected void registerTags(UpdaterContext context) {
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
    }

    protected void registerBeforeAfter(UpdaterContext context) {
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
    }

    protected void registerVisitorsByMethodAnnotations(final UpdaterContext context) {

        Method[] methods = getClass().getMethods();

        for (final Method method : methods) {
            PathVisitor pathVisitor = method.getAnnotation(PathVisitor.class);
            QueryVisitor queryVisitor = method.getAnnotation(QueryVisitor.class);

            if (pathVisitor != null) {
                final String path = pathVisitor.path();
                log.debug("registering path visitor on '{}' to invoke '{}'", path, method);
                context.registerVisitor(new UpdaterItemVisitor.PathVisitor(path) {
                    @Override
                    protected void leaving(Node node, int level) throws RepositoryException {
                        try {
                            method.invoke(this, node);
                        } catch (Exception e) {
                            handleException(e, node, path);
                        }
                    }
                });
                log.debug("registered path visitor on '{}' to invoke '{}'", path, method);
            }

            if (queryVisitor != null) {
                final String language = queryVisitor.language();
                final String query = queryVisitor.query();
                log.debug("registering query visitor on '{}' to invoke '{}'", query, method);
                context.registerVisitor(new UpdaterItemVisitor.QueryVisitor(query, language) {
                    @Override
                    protected void leaving(Node node, int level) throws RepositoryException {
                        try {
                            method.invoke(this, node);
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
