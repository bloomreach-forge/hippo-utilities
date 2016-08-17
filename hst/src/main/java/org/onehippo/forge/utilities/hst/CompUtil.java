/*
 * Copyright 2012-2016 Hippo B.V. (http://www.onehippo.com)
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

package org.onehippo.forge.utilities.hst;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.hippoecm.hst.component.support.bean.BaseHstComponent;
import org.hippoecm.hst.content.beans.standard.HippoBean;
import org.hippoecm.hst.core.component.HstRequest;

public class CompUtil {

    private static final String CLEANUP_CHARS = "[\f\n\r\t]";

    private CompUtil() {
        // prevent instantiation
    }

    public static HippoBean getBean(final BaseHstComponent comp, final HstRequest request, final String path) {

        if (path == null) {
            return request.getRequestContext().getContentBean();
        }

        if (path.startsWith("/")) {
            return request.getRequestContext().getSiteContentBaseBean().getBean(path.substring(1));
        }

        HippoBean currentBean = request.getRequestContext().getContentBean();
        if (currentBean != null) {
            return currentBean.getBean(path);
        }

        return null;
    }

    /**
     * Get a string from a configuration parameter, returning a default value if
     * the parameter is not there.
     */
    public static String getParameter(final BaseHstComponent comp, final HstRequest request, final String paramName, final String defaultValue) {

        final String value = comp.getComponentParameter(paramName);
        return (value != null) ?  value.trim() : defaultValue;
    }

    /**
     * Get a string List from comma-separated values of a configuration parameter.
     */
    public static List<String> getParameterList(final BaseHstComponent comp, final HstRequest request, final String paramName) {

        String commaSepValues = comp.getComponentParameter(paramName);

        if (commaSepValues == null) {
            return Collections.emptyList();
        }


        String[] values = commaSepValues.split(",");
        List<String> list = new ArrayList<String>(values.length);
        for (String value : values) {
            list.add(value.trim().replaceAll(getCleanUpChars(), ""));
        }
        return list;
    }

    /**
     * Get an int from a configuration parameter, returning a default value in
     * case of error or if the parameter is not there.
     */
    public static int getParameterInt(final BaseHstComponent comp, final HstRequest request, final String paramName, final int defaultValue) {

        final String paramValue = comp.getComponentParameter(paramName);
        if (paramValue != null) {
            try {
                return Integer.parseInt(paramValue.trim());
            }
            catch (NumberFormatException nfe) {
                return defaultValue;
            }
        }

        return defaultValue;
    }

    /**
     * Get a boolean from a configuration parameter, returning 'false' in case
     * of error or if the parameter is not there.
     */
    public static boolean getParameterBoolean(final BaseHstComponent comp, final HstRequest request, final String paramName) {
        return getParameterBoolean(comp, request, paramName, false);
    }

    /**
     * Get a boolean from a configuration parameter, returning a default value
     * if the parameter is not there, and false if the parsing fails.
     */
    public static boolean getParameterBoolean(final BaseHstComponent comp, HstRequest request, String paramName, boolean defaultValue) {
        final String paramValue = comp.getComponentParameter(paramName);
        if (paramValue != null) {
            return Boolean.valueOf(paramValue.trim()).booleanValue();
        }
        return defaultValue;
    }

    /**
     * Get a local configuration parameter, i.e. not overridden by parent
     * components, returning a default value if the parameter is not there.
     */
    public static String getLocalParameter(final BaseHstComponent comp, final HstRequest request,
                final String paramName, final String defaultValue) {

        final String value = comp.getComponentLocalParameter(paramName);
        return (value != null) ? value.trim() : defaultValue;
    }

    /**
     * Get a local configuration parameter as integer, i.e. not overridden by
     * parent components, returning a default value if the parameter is not
     * there or if parsing fails.
     */
    public static int getLocalParameterInt(final BaseHstComponent comp, final HstRequest request,
            final String paramName, final int defaultValue) {

        final String value = comp.getComponentLocalParameter(paramName);
        if (value != null) {
            try {
                return Integer.parseInt(value.trim());
            }
            catch (NumberFormatException nfe) {
                return defaultValue;
            }
        }

        return defaultValue;
    }

    /**
     * Get a local configuration parameter as boolean, i.e. not overridden by
     * parent components, returning a default value if the parameter is not
     * there.
     */
    public static boolean getLocalParameterBoolean(final BaseHstComponent comp, final HstRequest request,
            final String paramName, final boolean defaultValue) {
        final String paramValue = comp.getComponentLocalParameter(paramName);
        if (paramValue != null) {
            return Boolean.valueOf(paramValue.trim()).booleanValue();
        }
        return defaultValue;
    }

    /**
     * Get a public request parameter, returning a default value if the
     * parameter is not there.
     */
    public static String getPublicRequestParameter(final BaseHstComponent comp, final HstRequest request,
                final String paramName, final String defaultValue) {
        final String value = comp.getPublicRequestParameter(request, paramName);
        return (value != null) ? value.trim() : defaultValue;
    }

    /**
     * Get a public request parameter as integer, returning a default value in
     * case of error if the parameter is not there.
     */
    public static int getPublicRequestParameterInt(final BaseHstComponent comp, final HstRequest request,
                final String paramName, final int defaultValue) {
        final String value = comp.getPublicRequestParameter(request, paramName);

        if (value != null) {
            try {
                return Integer.parseInt(value.trim());
            }
            catch (NumberFormatException nfe) {
                return defaultValue;
            }
        }

        return defaultValue;
    }

    /**
     * Returns an array of values for a public request parameter. For use of multiple checkboxes
     * @param request
     * @param paramName
     * @return String[] value of the request parameter. null if the parameter does not exist or is empty.
     * @deprecated please use HST native BaseHstComponent#getPublicRequestParameters()
     */
    public String[] getPublicRequestParameters(final BaseHstComponent comp, final HstRequest request, final String paramName) {

        String contextNamespaceReference = request.getRequestContext().getContextNamespace();

        if (contextNamespaceReference == null) {
            contextNamespaceReference = "";
        }

        final Map<String, String []> namespaceLessParameters = request.getParameterMap(contextNamespaceReference);
        return namespaceLessParameters.get(paramName);
    }

    /**
     * Get a request parameter as integer, returning a default value if the
     * parameter is not there or if parsing fails.
     */
    public static int getRequestParameterInt(final HstRequest request, final String paramName, final int defaultValue) {

        final String paramValue = request.getParameter(paramName);
        if (paramValue != null) {
            try {
                return Integer.parseInt(paramValue.trim());
            }
            catch (NumberFormatException nfe) {
                return defaultValue;
            }
        }

        return defaultValue;
    }

    protected static String getCleanUpChars() {
        return CLEANUP_CHARS;
    }
}
