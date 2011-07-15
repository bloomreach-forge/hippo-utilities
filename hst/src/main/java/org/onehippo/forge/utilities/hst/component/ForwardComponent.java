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

public class ForwardComponent extends BaseHstComponent {

    public static final String FORWARD_PARAM = "forward";

    @Override
    public void doBeforeRender(HstRequest request, HstResponse response) throws HstComponentException {
        
        final String forward = getParameter(FORWARD_PARAM, request);
        
        if (forward == null || forward.length() == 0) {
            throw new HstComponentException("Parameter '" + FORWARD_PARAM + "' is required for " + this.getClass().getName());
        }
        
        try {
            response.forward(forward);
        }
        catch (IOException e) {
            throw new HstComponentException("Failed forwarding to " + forward, e);
        }
    }
}
