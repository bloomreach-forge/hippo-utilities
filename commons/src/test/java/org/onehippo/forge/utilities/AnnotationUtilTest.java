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

package org.onehippo.forge.utilities;

import java.lang.reflect.Method;
import java.util.Collection;

import org.onehippo.forge.utilities.commons.AnnotationUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Test;

import static org.testng.AssertJUnit.assertTrue;

/**
 * AnnotationUtilTest
 */
public class AnnotationUtilTest {
    @SuppressWarnings("unused")
    private static Logger log = LoggerFactory.getLogger(AnnotationUtilTest.class);

    @Test
    public void testFindClass() throws Exception {
        Class<Object> clazz = AnnotationUtil.findClass(AnnotationUtilTest.class.getName());
        assertTrue(clazz != null);
    }

    @Test
    public void testGetClassMethods() throws Exception {
        Collection<Method> methods = AnnotationUtil.getMethods(AnnotationUtilTest.class);
        int i = methods.size();
        assertTrue(methods.size() == 12);
    }
}
