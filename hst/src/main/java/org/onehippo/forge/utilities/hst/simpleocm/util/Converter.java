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

package org.onehippo.forge.utilities.hst.simpleocm.util;

import org.onehippo.forge.utilities.hst.simpleocm.build.PropertyBuilder;
import org.onehippo.forge.utilities.hst.simpleocm.load.FieldSetter;

/**
 * A converter converts / marshals between a bean and JCR
 *
 * @see org.onehippo.forge.utilities.hst.simpleocm.load.FieldSetter
 * @see org.onehippo.forge.utilities.hst.simpleocm.build.PropertyBuilder
 */
public interface Converter extends PropertyBuilder, FieldSetter {
    // combining the interfaces
}
