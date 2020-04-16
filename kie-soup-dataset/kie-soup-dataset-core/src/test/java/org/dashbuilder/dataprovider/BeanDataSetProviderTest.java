package org.dashbuilder.dataprovider;

import org.dashbuilder.dataset.DataSet;
import org.dashbuilder.dataset.DataSetGenerator;
import org.dashbuilder.dataset.DataSetLookup;
import org.dashbuilder.dataset.def.BeanDataSetDef;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class BeanDataSetProviderTest {

    public static final DataSetProviderType TYPE = () -> "BEAN";
    private static final String ACTIVE_BRANCH = "activeBranch";

    @Mock
    StaticDataSetProvider staticDataSetProvider;

    @InjectMocks
    BeanDataSetProvider beanDataSetProvider;

    public DataSetProviderType getType() {
        return DataSetProviderType.BEAN;
    }

    @Test
    public void testLookupDataSetWithNullDataSetLookup() throws Exception {
        final BeanDataSetDef def = Mockito.mock(BeanDataSetDef.class);
        final DataSetLookup lookup = Mockito.mock(DataSetLookup.class);
        final DataSet dataset = Mockito.mock(DataSet.class);
        BeanDataSetProvider beanDataSetProviderSpy = Mockito.spy(beanDataSetProvider);

        Mockito.when(lookup.getMetadata(Mockito.eq(ACTIVE_BRANCH))).thenReturn("test");
        Mockito.when(def.getProperty(Mockito.eq(ACTIVE_BRANCH))).thenReturn("test");
        Mockito.when(staticDataSetProvider.lookupDataSet(def.getUUID(),null))
                .thenReturn(dataset);

        DataSet result = beanDataSetProviderSpy.lookupDataSet(def, null);
        Mockito.verify(beanDataSetProviderSpy).isBranchChanged(Mockito.any());
        Assert.assertNull(result);
    }

    @Test
    public void testLookupDataSetWithDataSetLookup() throws Exception {
        final BeanDataSetDef def = Mockito.mock(BeanDataSetDef.class);
        final DataSetLookup lookup = Mockito.mock(DataSetLookup.class);
        final DataSet dataset = Mockito.mock(DataSet.class);
        final DataSetGenerator dataSetGenerator = Mockito.mock(DataSetGenerator.class);

        BeanDataSetProvider beanDataSetProviderSpy = Mockito.spy(beanDataSetProvider);

        Mockito.when(lookup.getMetadata(Mockito.eq(ACTIVE_BRANCH))).thenReturn("test");
        Mockito.when(def.getProperty(Mockito.eq(ACTIVE_BRANCH))).thenReturn("test");
        Mockito.when(def.getUUID()).thenReturn("uuid");
        Mockito.when(staticDataSetProvider.lookupDataSet(def.getUUID(),null))
                .thenReturn(dataset);
        Mockito.doReturn(dataSetGenerator)
                .when(beanDataSetProviderSpy)
                .lookupGenerator(Mockito.any());
        Mockito.doReturn(dataset).when(dataSetGenerator).buildDataSet(Mockito.any());

        DataSet result = beanDataSetProviderSpy.lookupDataSet(def, lookup);
        Mockito.verify(beanDataSetProviderSpy, Mockito.times(2)).isBranchChanged(Mockito.any());
        Assert.assertNull(result);
    }

    @Test
    public void testIsBranchChanged() {
        Assert.assertFalse(beanDataSetProvider.isBranchChanged(null));

        final DataSetLookup lookup = Mockito.mock(DataSetLookup.class);
        Assert.assertTrue(beanDataSetProvider.isBranchChanged(lookup));

        beanDataSetProvider.activeBranch = "test";
        Mockito.when(lookup.getMetadata(Mockito.eq(ACTIVE_BRANCH))).thenReturn("test");
        Assert.assertFalse(beanDataSetProvider.isBranchChanged(lookup));

        Mockito.when(lookup.getMetadata(Mockito.eq(ACTIVE_BRANCH))).thenReturn("master");
        Assert.assertTrue(beanDataSetProvider.isBranchChanged(lookup));
    }

}
