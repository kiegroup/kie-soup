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

package org.kie.soup.project.datamodel.oracle;

import org.assertj.core.api.Assertions;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Arrays;
import java.util.Collection;

@RunWith(Parameterized.class)
public class DataTypeTest {

    @Parameterized.Parameters(name = "Type={0}, isNumeric={1}, isDate={2}")
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][]{
                { DataType.TYPE_NUMERIC, true, false },
                { DataType.TYPE_NUMERIC_BYTE, true, false },
                { DataType.TYPE_NUMERIC_SHORT, true, false },
                { DataType.TYPE_NUMERIC_INTEGER, true, false },
                { DataType.TYPE_NUMERIC_LONG, true, false },
                { DataType.TYPE_NUMERIC_BIGINTEGER, true, false },
                { DataType.TYPE_NUMERIC_FLOAT, true, false },
                { DataType.TYPE_NUMERIC_DOUBLE, true, false },
                { DataType.TYPE_NUMERIC_BIGDECIMAL, true, false },
                { DataType.TYPE_BOOLEAN, false, false },
                { DataType.TYPE_COLLECTION, false, false },
                { DataType.TYPE_COMPARABLE, false, false },
                { DataType.TYPE_DATE, false, true },
                { DataType.TYPE_LOCAL_DATE, false, true },
                { DataType.TYPE_LOCAL_DATE_TIME, false, true },
                { DataType.TYPE_FINAL_OBJECT, false, false },
                { DataType.TYPE_OBJECT, false, false },
                { DataType.TYPE_STRING, false, false },
                { DataType.TYPE_THIS, false, false },
                { DataType.TYPE_VOID, false, false }
        });
    }

    @Parameterized.Parameter(0)
    public String dataType;

    @Parameterized.Parameter(1)
    public boolean isNumeric;

    @Parameterized.Parameter(2)
    public boolean isDate;

    @Test
    public void testIsNumeric() {
        Assertions.assertThat(DataType.isNumeric(dataType)).isEqualTo(isNumeric);
    }

    @Test
    public void testIsDate() {
        Assertions.assertThat(DataType.isDate(dataType)).isEqualTo(isDate);
    }

}