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

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;
import org.kie.soup.project.datamodel.oracle.FieldAccessorsAndMutators;
import org.kie.soup.project.datamodel.oracle.ModelField;
import org.kie.soup.project.datamodel.oracle.ModelField.FIELD_CLASS_TYPE;
import org.kie.soup.project.datamodel.oracle.ModelField.FIELD_ORIGIN;
import org.kie.soup.project.datamodel.oracle.ModuleDataModelOracle;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.kie.soup.project.datamodel.commons.oracle.ModuleDataModelOracleUtils.getFieldFullyQualifiedClassName;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ModuleDataModelOracleUtilsTest {

    @Test
    public void getFieldFullyQualifiedClassNameTest() {

        ModuleDataModelOracle mockedDMO = mock(ModuleDataModelOracle.class);
        Map<String, ModelField[]> moduleModelFields = new HashMap<>();

        // non-existent field for unknown class
        when(mockedDMO.getModuleModelFields()).thenReturn(moduleModelFields);
        String fullyQualifiedClassName = this.getClass().getName();
        String fieldName = "nonExistentField";

        String fqnFieldClassName = getFieldFullyQualifiedClassName(mockedDMO, fullyQualifiedClassName, fieldName);
        assertNull("Expected a null FQN field class name", fqnFieldClassName);

        // non-existent field for known class
        moduleModelFields.put(fullyQualifiedClassName, new ModelField[]{
                new ModelField("existentField",
                               String.class.getName(),
                               FIELD_CLASS_TYPE.REGULAR_CLASS,
                               FIELD_ORIGIN.DECLARED,
                               FieldAccessorsAndMutators.ACCESSOR,
                               null)// forgot what goes in here?
        });
        when(mockedDMO.getModuleModelFields()).thenReturn(moduleModelFields);

        fqnFieldClassName = getFieldFullyQualifiedClassName(mockedDMO, fullyQualifiedClassName, fieldName);
        assertNull("Expected a null FQN field class name", fqnFieldClassName);

        // existent field for known class
        fieldName = "testField";
        String fieldType = "org.acme.test.field.type";
        moduleModelFields.put(fullyQualifiedClassName, new ModelField[]{
                new ModelField("existentField",
                               String.class.getName(),
                               FIELD_CLASS_TYPE.REGULAR_CLASS,
                               FIELD_ORIGIN.DECLARED,
                               FieldAccessorsAndMutators.ACCESSOR,
                               null),// forgot what goes in here?
                new ModelField(fieldName,
                               fieldType,
                               FIELD_CLASS_TYPE.REGULAR_CLASS,
                               FIELD_ORIGIN.DECLARED,
                               FieldAccessorsAndMutators.ACCESSOR,
                               null) // forgot what goes in here?
        });
        when(mockedDMO.getModuleModelFields()).thenReturn(moduleModelFields);

        fqnFieldClassName = getFieldFullyQualifiedClassName(mockedDMO, fullyQualifiedClassName, fieldName);
        assertEquals("Expected a null FQN field class name", fieldType, fqnFieldClassName);
    }
}
