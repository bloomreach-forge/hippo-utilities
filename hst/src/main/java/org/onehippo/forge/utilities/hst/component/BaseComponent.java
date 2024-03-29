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

package org.onehippo.forge.utilities.hst.component;

import org.hippoecm.hst.component.support.bean.BaseHstComponent;
import org.hippoecm.hst.core.component.HstComponentException;
import org.hippoecm.hst.core.component.HstRequest;
import org.hippoecm.hst.core.component.HstResponse;

public class BaseComponent extends BaseHstComponent {

    private static final String CSSCLASS = "cssclass";
    private static final String ID = "id";

    @Override
    public void doBeforeRender(HstRequest request, HstResponse response) throws HstComponentException {
        super.doBeforeRender(request, response);

        final String cssClass = this.getComponentLocalParameter(CSSCLASS);
        if (cssClass != null) {
            request.setAttribute(CSSCLASS, cssClass);
        }
        
        final String id = this.getComponentLocalParameter(ID);
        if (id != null) {
            request.setAttribute(ID, id);
        }
    }
}
