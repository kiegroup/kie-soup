/*
 * Copyright 2018 Red Hat, Inc. and/or its affiliates.
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

package org.kie.soup.commons.util;

import java.util.Map;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class MapsTest {

    @Test
    public void testEmptyMap() {
        assertTrue(new Maps.Builder<>().build().isEmpty());
    }

    @Test
    public void testNonEmptyMap() {
        final String KEY_1 = "some key value";
        final String KEY_2 = "different key value";
        final Integer VALUE_1 = 15;
        final Integer VALUE_2 = 47;

        Map<String, Integer> map = new Maps.Builder<String, Integer>()
                .put(KEY_1, VALUE_1)
                .put(KEY_2, VALUE_2)
                .build();

        assertEquals(2, map.size());
        assertEquals(VALUE_1, map.get(KEY_1));
        assertEquals(VALUE_2, map.get(KEY_2));
    }

    @Test
    public void testDifferentTypes() {
        final Double KEY_1 = 1.1d;
        final Double KEY_2 = 41.2;
        final String VALUE_1 = "some value";
        final String VALUE_2 = "different value";

        Map<Double, String> map = new Maps.Builder<Double, String>()
                .put(KEY_1, VALUE_1)
                .put(KEY_2, VALUE_2)
                .build();

        assertEquals(2, map.size());
        assertEquals(VALUE_1, map.get(KEY_1));
        assertEquals(VALUE_2, map.get(KEY_2));
    }
}
