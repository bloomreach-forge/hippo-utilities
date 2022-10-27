/*
 * Copyright 2012-2022 Bloomreach
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

import org.onehippo.forge.utilities.hst.simpleocm.JcrNodeType;
import org.onehippo.forge.utilities.hst.simpleocm.JcrPath;

@JcrNodeType("jcr:extraagenda")
public class ExtraAgenda extends EmptyAgenda {

    @JcrPath("jcr:extraAgendaProperty")
    private String extraAgendaProperty;

    public String getExtraAgendaProperty() {
        return extraAgendaProperty;
    }

    public void setExtraAgendaProperty(final String extraAgendaProperty) {
        this.extraAgendaProperty = extraAgendaProperty;
    }
}
