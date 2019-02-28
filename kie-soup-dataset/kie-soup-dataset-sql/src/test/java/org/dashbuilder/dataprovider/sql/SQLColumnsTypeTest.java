/*
 * Copyright 2019 Red Hat, Inc. and/or its affiliates.
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

package org.dashbuilder.dataprovider.sql;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.sql.SQLException;
import java.sql.Statement;

import org.dashbuilder.dataset.DataColumn;
import org.dashbuilder.dataset.DataSet;
import org.dashbuilder.dataset.def.SQLDataSetDef;
import org.junit.Test;

public class SQLColumnsTypeTest extends SQLDataSetTestBase {
    

    String CLOB_TABLE = "CLOB_TABLE";
    String CLOB_COLUMN = "CLOB_CL";
    String CLOB_VAL = "TEST_CLOB";

    @Test
    public void clobColumnTest() throws Exception {
        prepareForClobTest();
        
        SQLDataSetDef def = new SQLDataSetDef();
        def.setDbTable(CLOB_TABLE);
        
        def.setDataSource("jdbc:h2:mem:test");
        dataSetDefRegistry.registerDataSetDef(def);
        
        DataSet ds = sqlDataSetProvider.lookupDataSet(def, null);
        
        DataColumn clobColumn = ds.getColumnById(CLOB_COLUMN);
        assertNotNull(clobColumn);
        assertEquals(1, clobColumn.getValues().size());
        Object object = clobColumn.getValues().get(0);
        assertEquals(CLOB_VAL, object.toString());
    }

    private void prepareForClobTest() throws SQLException {
        String TABLE_SQL = "CREATE TABLE "+ CLOB_TABLE +" ("
                + "ID INTEGER PRIMARY KEY,"
                + CLOB_COLUMN + " CLOB);";
        String INSERT = "INSERT INTO " + CLOB_TABLE + " VALUES(1, '"+ CLOB_VAL + "')";
        Statement stmt = conn.createStatement();
        stmt.executeUpdate(TABLE_SQL);
        stmt.executeUpdate(INSERT);
    }
    
}
