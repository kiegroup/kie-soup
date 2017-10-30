/*
 * Copyright 2017 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package org.kie.soup.project.datamodel.commons.oracle;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.kie.soup.project.datamodel.oracle.ExtensionKind;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.junit.Assert.assertEquals;

@RunWith(MockitoJUnitRunner.class)
public class PackageDataModelOracleImplTest {

    private static final String ELEMENT = "element";

    @Mock
    private ExtensionKind<String> kind;

    private PackageDataModelOracleImpl oracle;

    @Before
    public void setup() {
        this.oracle = new PackageDataModelOracleImpl();
    }

    @Test
    public void testAddExtensionsList() {
        oracle.addExtensions(kind,
                             Collections.singletonList(ELEMENT));

        assertEquals(1,
                     oracle.getExtensions(kind).size());
        assertEquals(ELEMENT,
                     oracle.getExtensions(kind).get(0));
    }

    @Test
    public void testAddExtensionsMap() {
        oracle.addExtensions(new HashMap<ExtensionKind<?>, List<?>>() {{
            put(kind,
                Collections.singletonList(ELEMENT));
        }});

        assertEquals(1,
                     oracle.getExtensions(kind).size());
        assertEquals(ELEMENT,
                     oracle.getExtensions(kind).get(0));
    }
}
