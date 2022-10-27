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

package org.onehippo.forge.utilities.hst.cookie;

import javax.servlet.http.Cookie;

import org.hippoecm.hst.core.component.HstRequest;
import org.hippoecm.hst.core.component.HstResponse;

/**
 * HstCookie, cookie helper class
 *
 * @version $Id: HstCookie.java 92541 2010-08-05 10:22:01Z mmilicevic $
 */
public class HstCookie {

    private static final String COOKIE_ENABLED_CHECK = "hstCookieCheck";

    /**
     * Constructor
     */
    private HstCookie() {
    }

    /**
     * Check if browser is accepting cookies
     * NOTE: we need at least two request to decide if client is accepting cookies so this method is "partially" useful
     * (e.g. when checking for cookies when page is loaded with ajax)
     *
     * @return true if accepting
     */
    public static boolean acceptsCookies(final HstRequest request, final HstResponse response) {
        Cookie cookie = getCookie(request, COOKIE_ENABLED_CHECK);
        if (cookie != null) {
            return true;
        }
        setSessionCookie(response, COOKIE_ENABLED_CHECK, COOKIE_ENABLED_CHECK);
        return false;
    }


    public static void removeCookie(final HstRequest request, final String name) {
        Cookie cookie = getCookie(request, name);
        if (cookie != null) {
            cookie.setMaxAge(0);
        }
    }


    public static Cookie getCookie(final HstRequest request, final String name) {
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                String cookieName = cookie.getName();
                if (name.equals(cookieName)) {
                    return cookie;
                }
            }
        }
        return null;
    }


    /**
     * Sets cookie which will expire after browser is closed
     *
     * @param response the HST response
     * @param name  cookie name
     * @param value cookie value
     */
    public static void setSessionCookie(final HstResponse response, String name, String value) {
        setCookie(response, name, value, -1);
    }

    /**
     * Set a cookie without path (or domain)
     *
     * @param response the HST response
     * @param name  cookie name
     * @param value cookie value
     * @param age   cookie age in seconds
     */
    public static void setCookie(final HstResponse response, String name, String value, int age) {
        setCookie(response, name, value, age, null, null, false);
    }


    /**
     * Creates and sets the cookie onto response
     *
     * @param response the HST response
     * @param name   cookie name
     * @param value  cookie value
     * @param age    cookie age in seconds
     * @param domain cookie domain (optional)
     * @param path   cookie path, defaults to root ('/')
     * @param secure is cookie secure
     */
    public static void setCookie(final HstResponse response, final String name, final String value, final int age, final String domain, final String path, final boolean secure) {
        Cookie cookie = new Cookie(name, value);
        if (path != null) {
            cookie.setPath(path);
        } else {
            cookie.setPath("/");
        }
        if (domain != null) {
            cookie.setDomain(domain);
        }
        cookie.setSecure(secure);
        cookie.setMaxAge(age);
        response.addCookie(cookie);
    }
}

