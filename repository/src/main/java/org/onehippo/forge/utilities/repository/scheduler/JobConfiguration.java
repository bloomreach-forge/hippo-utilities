package org.onehippo.forge.utilities.repository.scheduler;

import java.util.Date;

import javax.jcr.Node;
import javax.jcr.Property;
import javax.jcr.PropertyIterator;
import javax.jcr.RepositoryException;

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

    public Date getDate(final String propertyName) {
        return NodeUtils.getDate(node, propertyName);
    }

    public Double getDouble(final String propertyName) {
        return NodeUtils.getDouble(node, propertyName);
    }

    public Long getLong(final String propertyName) {
        return NodeUtils.getLong(node, propertyName);
    }

    public String getString(final String propertyName) {
        return NodeUtils.getString(node, propertyName);
    }

    public String getString(final String propertyName, final String defaultValue) {
        return NodeUtils.getString(node, propertyName, defaultValue);
    }

    public String[] getStrings(final String propertyName, final String[] defaultValues) {
        return NodeUtils.getStrings(node, propertyName, defaultValues);
    }

    // for debugging and logging
    @Override
    public String toString() {
        final StringBuilder builder = new StringBuilder();
        try {
            if (node != null) {
                PropertyIterator it = node.getProperties();
                while (it.hasNext()) {
                    final Property property = it.nextProperty();
                    builder.append(property.getDefinition().getName());
                    builder.append("=");
                    builder.append(property.getValue());
                    if (it.hasNext()) {
                        builder.append(", ");
                    }
                }
            }
        } catch (RepositoryException e) {
            builder.append("RepositoryException in toString of ").append(JobConfiguration.class.getName()).append(": ").append(e.getMessage());
        }
        return builder.toString();
    }
}
