/*
 * Copyright 2012 Hippo
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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.hippoecm.hst.core.component.HstComponentException;
import org.hippoecm.hst.core.component.HstRequest;
import org.hippoecm.hst.core.component.HstResponse;
import org.onehippo.forge.utilities.hst.CompUtil;

public class Container extends BaseComponent {

    private static final String INCLUDES = "includes";
    private static final String SORTING = "sorting";
    private static final String SORTING_DEFAULT = "default";

    @Override
    public void doBeforeRender(HstRequest request, HstResponse response) throws HstComponentException {

        super.doBeforeRender(request, response);

        // dynamically include component children
        List<String> childNames = response.getChildContentNames();
        if (childNames != null && childNames.size() > 0) {
            
            childNames = sortChildNames(childNames, request, response);
            
            request.setAttribute(INCLUDES, childNames);
        }
    }

    protected List<String> sortChildNames(List<String> childNames, HstRequest request, HstResponse response) {
        
        // default behaviour is sorted alphabetically
        final String sorting = CompUtil.getLocalParameter(this, request, SORTING, SORTING_DEFAULT);
        
        if (SORTING_DEFAULT.equals(sorting)) {
            List<String> names = new ArrayList<String>();
            names.addAll(childNames);
            Collections.sort(names);
            return names;
        }

        final List<String> customSortedNames = customSortChildNames(childNames, request, response);
        if (customSortedNames != null) {
            return customSortedNames;
        }
        
        // no sorting
        return childNames;
    }

    protected List<String> customSortChildNames(List<String> childNames, HstRequest request, HstResponse response) {
        return null;
    }
}
