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

package org.onehippo.forge.utilities.commons;

import java.math.BigDecimal;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import javax.jcr.Node;
import javax.jcr.Property;
import javax.jcr.RepositoryException;
import javax.jcr.Value;

/**
 * Utility to read JCR properties.
 */
public class NodeUtils {

    /**
     * Private constructor preventing instantiation.
     */
    private NodeUtils() {
    }

    /**
     * Get a boolean property from a node, returning false if not found
     */
    public static boolean getBoolean(final Node node, final String propertyName) {
        return getBoolean(node, propertyName, false/*defaultValue*/);
    }

    /**
     * Get a boolean property from a node, returning a default value if not found
     */
    public static boolean getBoolean(final Node node, final String propertyName, final boolean defaultValue) {
        if (node != null) {
            try {
                if (node.hasProperty(propertyName)) {
                    final Property property = node.getProperty(propertyName);
                    if (property != null) {
                        return property.getValue().getBoolean();
                    }
                }
            } catch (RepositoryException e) {
                throw new IllegalStateException(e);
            }
        }
        return defaultValue;
    }

    /**
     * Get a Date property from a node, returning null if not found
     */
    public static Date getDate(final Node node, final String propertyName) {
        return getDate(node, propertyName, null/*defaultValue*/);
    }

    /**
     * Get a Date property from a node, returning a default value if not found
     */
    public static Date getDate(final Node node, final String propertyName, final Date defaultValue) {
        if (node != null) {
            try {
                if (node.hasProperty(propertyName)) {
                    final Property property = node.getProperty(propertyName);
                    if (property != null) {
                        return property.getValue().getDate().getTime();
                    }
                }
            } catch (RepositoryException e) {
                throw new IllegalStateException(e);
            }
        }
        return defaultValue;
    }

    /**
     * Get a decimal property from a node, returning null if not found
     */
    public static BigDecimal getDecimal(final Node node, final String propertyName) {
        return getDecimal(node, propertyName, null/*defaultValue*/);
    }

    /**
     * Get a decimal property from a node, returning a default value if not found
     */
    public static BigDecimal getDecimal(final Node node, final String propertyName, final BigDecimal defaultValue) {
        if (node != null) {
            try {
                if (node.hasProperty(propertyName)) {
                    final Property property = node.getProperty(propertyName);
                    if (property != null) {
                        return property.getValue().getDecimal();
                    }
                }
            } catch (RepositoryException e) {
                throw new IllegalStateException(e);
            }
        }
        return defaultValue;
    }

    /**
     * Get a double property from a node, returning null if not found
     */
    public static Double getDouble(final Node node, final String propertyName) {
        return getDouble(node, propertyName, null/*defaultValue*/);
    }

    /**
     * Get a double property from a node, returning a default value if not found
     */
    public static Double getDouble(final Node node, final String propertyName, final Double defaultValue) {
        if (node != null) {
            try {
                if (node.hasProperty(propertyName)) {
                    final Property property = node.getProperty(propertyName);
                    if (property != null) {
                        return property.getValue().getDouble();
                    }
                }
            } catch (RepositoryException e) {
                throw new IllegalStateException(e);
            }
        }
        return defaultValue;
    }

    /**
     * Get a long property from a node, returning null if not found
     */
    public static Long getLong(final Node node, final String propertyName) {
        return getLong(node, propertyName, null/*defaultValue*/);
    }

    /**
     * Get a long property from a node, returning a default value if not found
     */
    public static Long getLong(final Node node, final String propertyName, final Long defaultValue) {
        if (node != null) {
            try {
                if (node.hasProperty(propertyName)) {
                    final Property property = node.getProperty(propertyName);
                    if (property != null) {
                        return property.getValue().getLong();
                    }
                }
            } catch (RepositoryException e) {
                throw new IllegalStateException(e);
            }
        }
        return defaultValue;
    }

    /**
     * Get a String property from a node, returning null if not found
     */
    public static String getString(final Node node, final String propertyName) {
        return getString(node, propertyName, null);
    }

    /**
     * Get a String property from a node, returning a default value if not found
     */
    public static String getString(final Node node, final String propertyName, final String defaultValue) {
        if (node != null) {
            try {
                if (node.hasProperty(propertyName)) {
                    final Property property = node.getProperty(propertyName);
                    if (property != null) {
                        return property.getValue().getString();
                    }
                }
            } catch (RepositoryException e) {
                return defaultValue;
            }
        }
        return defaultValue;
    }

    /**
     * Get a String property from a node, returning null if not found
     */
    public static String[] getStrings(final Node node, final String propertyName) {
        return getStrings(node, propertyName, null/*defaultValue*/);
    }

    /**
     * Get a String property from a node, returning a default value if not found
     */
    public static String[] getStrings(final Node node, final String propertyName, final String[] defaultValue) {
        if (node != null) {
            try {
                if (node.hasProperty(propertyName)) {
                    final Property property = node.getProperty(propertyName);
                    if (property != null && !property.isMultiple()) {
                        return defaultValue;
                    }
                    if (property != null) {
                        List<String> values = new LinkedList<String>();
                        for (Value value : property.getValues()) {
                            values.add(value.getString());
                        }
                        return values.toArray(new String[values.size()]);
                    }
                }
            } catch (RepositoryException e) {
                throw new IllegalStateException(e);
            }
        }
        return defaultValue;
    }
}
