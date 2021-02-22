/*
 * Copyright 2021 Red Hat, Inc. and/or its affiliates.
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
 */
package org.dashbuilder.dataprovider.sql;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.when;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.dashbuilder.dataprovider.sql.dialect.Dialect;
import org.dashbuilder.dataprovider.sql.model.Column;
import org.dashbuilder.dataset.ColumnType;
import org.dashbuilder.dataset.DataSetMetadata;
import org.dashbuilder.dataset.def.DataSetDefFactory;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.modules.junit4.PowerMockRunnerDelegate;

@RunWith(PowerMockRunner.class)
@PowerMockRunnerDelegate(Parameterized.class)
@PrepareForTest({JDBCUtils.class})
public class SQLDataSetMetadataLimitTest {

    @Parameters(name = "Dialect : {0}")
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][] {
                 {JDBCUtils.DB2, "FETCH FIRST 0 ROWS ONLY"}, 
                 {JDBCUtils.DEFAULT, "LIMIT 0"},
                 {JDBCUtils.H2, "LIMIT 0"},
                 {JDBCUtils.MYSQL, "LIMIT 0"},
                 {JDBCUtils.ORACLE, "FETCH FIRST 0 ROWS ONLY"},
                 {JDBCUtils.POSTGRES, "LIMIT 0"},
                 {JDBCUtils.SQLSERVER, "TOP 0"},
                 {JDBCUtils.SYBASE_ASE, "TOP 0"}
           });
    }
    
    @Mock
    Statement statement;
    
    @Mock
    ResultSet resultSet;
    
    SQLDataSetProvider dataSetProvider = SQLDataSetProvider.get();
    
    private final Dialect dialect;
    private final String limitAssertion;
    
    public SQLDataSetMetadataLimitTest(Dialect dialect, String limitAssertion) {
        this.dialect = dialect;
        this.limitAssertion = limitAssertion;
    }
    
    @Before
    public void setUp() throws Exception {
        PowerMockito.mockStatic(JDBCUtils.class);
        
        final List<Column> dbColumns = new ArrayList<Column>();
        dbColumns.add(SQLFactory.column("dbWins1", ColumnType.LABEL, 10));
        dbColumns.add(SQLFactory.column("dbWins2", ColumnType.LABEL, 10));
        dbColumns.add(SQLFactory.column("dbWins3", ColumnType.LABEL, 10));

        dataSetProvider.setDataSourceLocator(new DatabaseTestSettings().getDataSourceLocator());
        ResultSetHandler resultSetHandler = new ResultSetHandler(resultSet, statement);
        
        when(JDBCUtils.dialect(any(Connection.class))).thenReturn(dialect);
        when(JDBCUtils.executeQuery(any(Connection.class), any())).thenReturn(resultSetHandler);
        when(JDBCUtils.getColumns(any(),any())).thenReturn(dbColumns);
        when(JDBCUtils.fixCase(any(Connection.class),eq("test"))).thenReturn("TEST");
    }

    @Test
    public void testGetColumnsWithLimitZero() throws Exception {
        DataSetMetadata metadata = dataSetProvider.getDataSetMetadata(
                DataSetDefFactory.newSQLDataSetDef()
                        .uuid("test")
                        .estimateSize(false)
                        .dbTable("test", true)
                        .buildDef());

        ArgumentCaptor<Connection> conn = ArgumentCaptor.forClass(Connection.class);
        ArgumentCaptor<String> sql = ArgumentCaptor.forClass(String.class);

        PowerMockito.verifyStatic(JDBCUtils.class, atLeastOnce());
        JDBCUtils.executeQuery(conn.capture(), sql.capture());
        assertThat(sql.getValue().toUpperCase(), containsString(limitAssertion));
        
        List<String> columnIds = metadata.getColumnIds();
        assertEquals(3, columnIds.size());
    }
}
