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

package org.onehippo.forge.utilities.commons;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Static utility class for getting (annotated) fields and methods from classes, using reflection.
 */
public final class AnnotationUtil {

    private static Logger log = LoggerFactory.getLogger(AnnotationUtil.class);

    /**
     * Private constructor preventing instantiation.
     */
    private AnnotationUtil() {
    }

    /**
     * Find a class for given name, loaded from current thread's context class loader.
     *
     * @param name of the class
     * @return null if not found or Class if found
     */
    @SuppressWarnings({"unchecked"})
    public static <T> Class<T> findClass(final String name) {
        try {
            return (Class<T>) Thread.currentThread().getContextClassLoader().loadClass(name);
        } catch (ClassNotFoundException e) {
            log.error("No class found within class loader " + e);
        }
        return null;
    }

    /**
     * Get fields of an class which are annotated with specific annotation and set them accessible
     * (if necessary).
     *
     * @param clazz           class we are scanning for annotated fields.
     * @param annotationClass annotation we are interested in
     * @return a collection containing (accessible) Field objects (or an empty collection)
     */
    public static Collection<Field> getAnnotatedFields(final Class<?> clazz, final Class<? extends Annotation> annotationClass) {
        final Collection<Field> fields = getFields(clazz);
        final Iterator<Field> iterator = fields.iterator();

        while (iterator.hasNext()) {
            final Field field = iterator.next();
            if (!field.isAnnotationPresent(annotationClass)) {
                iterator.remove();
            } else if (!field.isAccessible()) {
                try {
                    field.setAccessible(true);
                } catch (SecurityException se) {
                    log.error("Security exception while setting accessible: " + se);
                }
            }
        }

        return fields;
    }

    /**
     * Get methods of an class which are annotated with specific annotation and set them accessible
     * (if necessary).
     *
     * @param clazz           class we are scanning for annotated methods.
     * @param annotationClass annotation we are interested in
     * @return a collection containing (accessible) Field objects (or an empty collection)
     */
    public static Collection<Method> getAnnotatedMethods(final Class<?> clazz, final Class<? extends Annotation> annotationClass) {
        final Collection<Method> methods = getMethods(clazz);
        final Iterator<Method> iterator = methods.iterator();

        while (iterator.hasNext()) {
            final Method method = iterator.next();
            if (!method.isAnnotationPresent(annotationClass)) {
                iterator.remove();
            } else if (!method.isAccessible()) {
                try {
                    method.setAccessible(true);
                } catch (SecurityException se) {
                    log.error("Security exception while setting accessible: " + se);
                }
            }
        }

        return methods;
    }

    /**
     * Get the declared fields of a class, including all fields of it's super classes.
     *
     * @param clazz class to scan for fields
     * @return collection of declared Field objects
     */
    public static Collection<Field> getFields(Class<?> clazz) {
        final Map<String, Field> fields = new HashMap<String, Field>();

        for (; clazz != null;) {
            for (Field field : clazz.getDeclaredFields()) {
                if (!fields.containsKey(field.getName())) {
                    fields.put(field.getName(), field);
                }
            }
            clazz = clazz.getSuperclass();
        }

        return fields.values();
    }


    /**
     * Get the declared methods of a class, including all methods of it's super classes.
     *
     * @param clazz class to scan for methods
     * @return collection of declared Method objects
     */
    public static Collection<Method> getMethods(Class<?> clazz) {
        final Map<String, Method> methods = new HashMap<String, Method>();

        for (; clazz != null;) {
            for (Method method : clazz.getDeclaredMethods()) {
                boolean isOverridden = false;
                for (Method overriddenMethod : methods.values()) {
                    if (overriddenMethod.getName().equals(method.getName()) && Arrays.deepEquals(method.getParameterTypes(), overriddenMethod.getParameterTypes())) {
                        isOverridden = true;
                        break;
                    }
                }
                if (!isOverridden) {
                    methods.put(method.getName(), method);
                }
            }
            clazz = clazz.getSuperclass();
        }

        return methods.values();
    }
}

