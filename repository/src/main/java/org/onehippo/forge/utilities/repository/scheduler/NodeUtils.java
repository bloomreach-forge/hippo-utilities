package org.onehippo.forge.utilities.repository.scheduler;

import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import javax.jcr.Node;
import javax.jcr.Property;
import javax.jcr.RepositoryException;
import javax.jcr.Value;

/**
 * Utility to read JCR properties.
 *
 * TODO FIXME move NodeUtils to commons
 */
public class NodeUtils {

    /**
     * Get a boolean property from a node, returning false if not found
     */
    public static boolean getBoolean(final Node node, final String propertyName) {
        return getBoolean(node, propertyName, false);
    }

    /**
     * Get a boolean property from a node, returning a default if not found
     */
    public static boolean getBoolean(final Node node, final String propertyName, final boolean defaultValue) {

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
        return defaultValue;
    }

    /**
     * Get a Date property from a node, returning a null if not found
     */
    public static Date getDate(final Node node, final String propertyName) {
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
        return null;
    }

    /**
     * Get a double property from a node, returning null if not found
     */
    public static Double getDouble(final Node node, final String propertyName) {
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
        return null;
    }

    /**
     * Get a long property from a node, returning null if not found
     */
    public static Long getLong(final Node node, final String propertyName) {
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
        return null;
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
        return defaultValue;
    }

    /**
     * Get a String property from a node, returning a default value if not found
     */
    public static String[] getStrings(final Node node, final String propertyName, final String defaultValue[]) {
        try {
            if (node.hasProperty(propertyName)) {
                final Property property = node.getProperty(propertyName);
                if (property != null && !property.isMultiple()) {
                    return null;
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
            return defaultValue;
        }
        return new String[]{};
    }
}
