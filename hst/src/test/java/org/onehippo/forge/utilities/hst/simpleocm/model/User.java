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
import java.util.HashMap;
import java.util.Map;

import org.onehippo.forge.utilities.hst.simpleocm.JcrNodeType;
import org.onehippo.forge.utilities.hst.simpleocm.JcrPath;

/**
 * @version $Id$
 */
@JcrNodeType(value = "jcrmockup:user")
public class User implements Serializable {

    private static final long serialVersionUID = -3487145516242048487L;

    @JcrPath(value = "jcrmockup:username")
    private String userName;

    @JcrPath(value = "jcrmockup:fullname")
    private String fullName;

    @JcrPath(value = "*")
    private Map<String, Attribute> attributes;

    @JcrPath(value = "*")
    private Map<String, Preference> preferences;

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public Map<String, Attribute> getAttributes() {
        return this.attributes;
    }

    public void setAttributes(HashMap<String, Attribute> attributes) {
        this.attributes.putAll(attributes);
    }

    public Map<String, Preference> getPreferences() {
        return this.preferences;
    }

    public void setPreferences(HashMap<String, Preference> preferences) {
        this.preferences = preferences;
    }
}
