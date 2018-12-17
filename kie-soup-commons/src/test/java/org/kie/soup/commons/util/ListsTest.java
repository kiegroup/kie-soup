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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class ListsTest {

    @Test
    public void testEmptyList() {
        assertTrue(new Lists.Builder<>().build().isEmpty());
    }

    @Test
    public void testNonEmptyList() {
        final String E_1 = "some key value";
        final String E_2 = "different key value";

        List<String> list = new Lists.Builder<String>()
                .add(E_1)
                .add(E_2)
                .build();

        assertEquals(2, list.size());
        assertTrue(list.contains(E_1));
        assertTrue(list.contains(E_2));
    }

    @Test
    public void testDifferentTypes() {
        final Double E_1 = 1.1d;
        final Double E_2 = 41.2;

        List<Double> list = new Lists.Builder<Double>()
                .add(E_1)
                .add(E_2)
                .build();

        assertEquals(2, list.size());
        assertTrue(list.contains(E_1));
        assertTrue(list.contains(E_2));
    }

    @Test
    public void testAddAll() {
        List<String> strings = new ArrayList<>();
        strings.add("first");
        strings.add("second");
        strings.add("third");

        List<String> tested = new Lists.Builder<String>()
                .addAll(strings).build();

        assertEquals(strings, tested);
    }

    @Test
    public void testAddAllFromCollection() {
        Collection<String> strings = new HashSet<>();
        strings.add("first");
        strings.add("second");
        strings.add("third");

        List<String> tested = new Lists.Builder<String>()
                .addAll(strings).build();

        assertThat(tested).containsSequence(strings);
        assertThat(tested.size()).isEqualTo(strings.size());
    }
}