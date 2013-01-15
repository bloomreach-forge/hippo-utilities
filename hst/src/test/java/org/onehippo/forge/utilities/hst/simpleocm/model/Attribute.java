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

package org.onehippo.forge.utilities.hst.simpleocm.model;

import java.io.Serializable;
import java.util.Date;

import org.onehippo.forge.utilities.hst.simpleocm.JcrNodeType;
import org.onehippo.forge.utilities.hst.simpleocm.JcrPath;

/**
 * @version $Id$
 */
@JcrNodeType(value = "jcrmockup:attribute")
public class Attribute implements Serializable {

    private static final long serialVersionUID = -8266827185512030814L;

    @JcrPath(value = "jcrmockup:key")
    private String key;

    @JcrPath(value = "jcrmockup:value")
    private String value;

    @JcrPath(value = "jcrmockup:readOnly")
    private boolean readOnly;

    @JcrPath(value = "jcrmockup:lastmodified")
    private Date lastModified;

    @JcrPath(value = "jcrmockup:lastmodifiedby")
    private String lastModifiedBy;

    @JcrPath(value = "jcrmockup:createdby")
    private String createdBy;

    public Attribute(String key, String value, boolean readOnly) {
        this.key = key;
        this.value = value;
        this.readOnly = readOnly;
        this.lastModified = new Date();
    }

    public String getKey() {
        return this.key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getValue() {
        return this.value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public boolean isReadOnly() {
        return readOnly;
    }

    public void setReadOnly(boolean readOnly) {
        this.readOnly = readOnly;
    }

    public Date getLastModified() {
        return (Date) lastModified.clone();
    }

    public void setLastModified(Date lastModified) {
        this.lastModified = (Date) lastModified.clone();
    }

    public String getLastModifiedBy() {
        return lastModifiedBy;
    }

    public void setLastModifiedBy(String lastModifiedBy) {
        this.lastModifiedBy = lastModifiedBy;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }
}
