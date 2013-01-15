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

package org.onehippo.forge.utilities.repository.scheduler;

import java.util.Date;

import javax.jcr.Node;
import javax.jcr.Property;
import javax.jcr.PropertyIterator;
import javax.jcr.RepositoryException;
import javax.jcr.Value;

import org.onehippo.forge.utilities.commons.NodeUtils;

/**
 * Configuration object for a job implementation.
 */
public class JobConfiguration {

    private final Node node;

    public JobConfiguration() throws RepositoryException {
        this.node = null;
    }

    public JobConfiguration(final Node node) throws RepositoryException {
        if (!node.isNodeType(Namespace.NodeType.JOB_CONFIGURATION)) {
            throw new IllegalStateException(String.format("node %s is not of type %s but %s", node.getPath(),
                    Namespace.NodeType.JOB_CONFIGURATION, ((node == null) ? "null" : node.getPrimaryNodeType())));
        }
        this.node = node;
    }

    public Boolean getBoolean(final String propertyName) {
        return NodeUtils.getBoolean(node, propertyName);
    }

    public Boolean getBoolean(final String propertyName, final Boolean defaultValue) {
        return NodeUtils.getBoolean(node, propertyName, defaultValue);
    }

    public Date getDate(final String propertyName) {
        return NodeUtils.getDate(node, propertyName);
    }

    public Date getDate(final String propertyName, final Date defaultValue) {
        return NodeUtils.getDate(node, propertyName, defaultValue);
    }

    public Double getDouble(final String propertyName) {
        return NodeUtils.getDouble(node, propertyName);
    }

    public Double getDouble(final String propertyName, final Double defaultValue) {
        return NodeUtils.getDouble(node, propertyName, defaultValue);
    }

    public Long getLong(final String propertyName) {
        return NodeUtils.getLong(node, propertyName);
    }

    public Long getLong(final String propertyName, final Long defaultValue) {
        return NodeUtils.getLong(node, propertyName, defaultValue);
    }

    public String getString(final String propertyName) {
        return NodeUtils.getString(node, propertyName);
    }

    public String getString(final String propertyName, final String defaultValue) {
        return NodeUtils.getString(node, propertyName, defaultValue);
    }

    public String[] getStrings(final String propertyName) {
        return NodeUtils.getStrings(node, propertyName);
    }

    public String[] getStrings(final String propertyName, final String[] defaultValues) {
        return NodeUtils.getStrings(node, propertyName, defaultValues);
    }

    // for debugging and logging
    @Override
    public String toString() {
        final StringBuilder builder = new StringBuilder(JobConfiguration.class.getSimpleName()).append("[");
        if (node != null) {
            try {
                builder.append("node=");
                builder.append(node.getPath());
                builder.append(", properties: ");
                PropertyIterator it = node.getProperties();
                while (it.hasNext()) {
                    final Property property = it.nextProperty();
                    builder.append(property.getDefinition().getName());
                    builder.append("=");
                    if (property.isMultiple()) {
                        builder.append("[");
                        Value[] values = property.getValues();
                        for (int i = 0;  i < values.length; i++) {
                            builder.append(values[i].getString());
                            if (i < values.length - 1) {
                                builder.append(", ");
                            }
                        }
                        builder.append("]");
                    }
                    else {
                        builder.append(property.getString());
                    }
                    if (it.hasNext()) {
                        builder.append(", ");
                    }
                }
            } catch (RepositoryException e) {
                builder.append("RepositoryException in toString of ").append(JobConfiguration.class.getName()).append(": ").append(e.getMessage());
            }
        } else {
            builder.append("node=null");
        }
        builder.append("]");
        return builder.toString();
    }
}
