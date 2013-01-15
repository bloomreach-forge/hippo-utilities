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

import java.util.Calendar;
import java.util.List;

import org.onehippo.forge.utilities.hst.simpleocm.JcrNodeType;
import org.onehippo.forge.utilities.hst.simpleocm.JcrPath;

@JcrNodeType(value = "jcrmockup:agenda")
public class Agenda {

    @JcrPath("jcrmockup:long")
    public Long longNumber;

    @JcrPath("jcrmockup:double")
    public Double doubleNumber;

    @JcrPath("jcrmockup:boolean")
    public Boolean booleanField;

    @JcrPath("jcrmockup:date")
    public Calendar calendar;

    @JcrPath(value = "jcrmockup:introduction")
    public String introduction;

    @JcrPath(value = "jcrmockup:body")
    public HippoHtml body;

    @JcrPath(value = "jcrmockup:title")
    public String title;

    @JcrPath(value = "jcrmockup:tags")
    public List<String> tags;

    @JcrPath(value = "jcrmockup:paragraph")
    public List<HippoHtml> paragraphs;

    @JcrPath(value = "hippo:paths")
    public List<String> paths;

    @JcrPath(value="jcrmockup:obscured", converter = StringObscurerConverter.class)
    public String obscuredString;

    public String getIntroduction() {
        return this.introduction;
    }

    public String getTitle() {
        return this.title;
    }

    public HippoHtml getBody() {
        return this.body;
    }

    public List<String> getTags() {
        return this.tags;
    }

    public List<HippoHtml> getParagraphs() {
        return this.paragraphs;
    }

    public Long getLongNumber() {
        return longNumber;
    }

    public void setLongNumber(final Long longNumber) {
        this.longNumber = longNumber;
    }

    public Double getDoubleNumber() {
        return doubleNumber;
    }

    public void setDoubleNumber(final Double doubleNumber) {
        this.doubleNumber = doubleNumber;
    }

    public Boolean getBooleanField() {
        return booleanField;
    }

    public void setBooleanField(final Boolean booleanField) {
        this.booleanField = booleanField;
    }

    public String getObscuredString() {
        return obscuredString;
    }

    public void setObscuredString(final String obscuredString) {
        this.obscuredString = obscuredString;
    }
}
