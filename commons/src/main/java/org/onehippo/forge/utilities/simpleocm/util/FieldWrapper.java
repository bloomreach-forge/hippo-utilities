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

package org.onehippo.forge.utilities.simpleocm.util;

import java.lang.reflect.Field;

/**
 * A wrapper to simplify the access of field
 */
public class FieldWrapper {

    private Field field;
    private Object object;

    public FieldWrapper(Object object, Field field) {
        this.field = field;
        this.object = object;
    }

    public Field getField() {
        return this.field;
    }

    public Object getFieldOwnerObject() {
        return this.object;
    }

    public void setValue(Object object) throws IllegalAccessException {
        if (!this.field.isAccessible()) {
            this.field.setAccessible(true);
        }
        this.field.set(this.object, object);
    }

    public Object getValue() throws IllegalAccessException{
        if (!this.field.isAccessible()) {
            this.field.setAccessible(true);
        }
        return this.field.get(this.object);
    }
}