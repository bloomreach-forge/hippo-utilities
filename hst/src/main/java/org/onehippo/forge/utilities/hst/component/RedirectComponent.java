/*
 * Copyright 2011 Hippo
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
package org.onehippo.forge.utilities.hst.component;

import java.io.IOException;

import org.hippoecm.hst.component.support.bean.BaseHstComponent;
import org.hippoecm.hst.core.component.HstComponentException;
import org.hippoecm.hst.core.component.HstRequest;
import org.hippoecm.hst.core.component.HstResponse;

public class RedirectComponent extends BaseHstComponent {

    public static final String REDIRECT_PARAM = "redirect";
    public static final String TYPE_PARAM = "type";

    public enum Type {
        component, response
    }

    @Override
    public void doBeforeRender(HstRequest request, HstResponse response) throws HstComponentException {
        
        final String redirect = getParameter(REDIRECT_PARAM, request);
        
        if (redirect == null || redirect.length() == 0) {
                throw new HstComponentException("Parameter '" + REDIRECT_PARAM + "' is required for " + this.getClass().getName());
        } 
        
        final String typeStr = getParameter(TYPE_PARAM, request);
        if (typeStr != null) {
            final Type type = Type.valueOf(typeStr);
            switch (type) {
                case response:
                    try {
                        response.sendRedirect(redirect);
                    }
                    catch (IOException e) {
                        throw new HstComponentException("Failed to redirect to " + redirect, e);
                    }
                    break;
                case component:
                    this.sendRedirect(redirect, request, response);
            }
        }
        
        this.sendRedirect(redirect, request, response);
    }
}
