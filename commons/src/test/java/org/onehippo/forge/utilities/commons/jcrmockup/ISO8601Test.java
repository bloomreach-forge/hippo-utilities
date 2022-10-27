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

package org.onehippo.forge.utilities.commons.jcrmockup;

import java.util.Calendar;

import org.testng.annotations.Test;

import static junit.framework.Assert.assertEquals;

/**
 * Test for {@link ISO8601}
 */
public class ISO8601Test {

    @Test
    public void testParse() {
        Calendar c = ISO8601.parse("1979-09-23T14:01:34.357+01:00");
        assertEquals(1979, c.get(Calendar.YEAR));
        assertEquals(8, c.get(Calendar.MONTH));
        assertEquals(23, c.get(Calendar.DAY_OF_MONTH));
        assertEquals(14, c.get(Calendar.HOUR_OF_DAY));
        assertEquals(01, c.get(Calendar.MINUTE));
        assertEquals(34, c.get(Calendar.SECOND));
        assertEquals(357, c.get(Calendar.MILLISECOND));
        assertEquals(1000 * 60 * 60, c.get(Calendar.ZONE_OFFSET));
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testParseNull() {
        ISO8601.parse(null);
    }

}
