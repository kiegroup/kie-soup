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
package org.dashbuilder.dataprovider.kafka;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.dashbuilder.dataprovider.kafka.metrics.KafkaMetricColllector;
import org.dashbuilder.dataprovider.kafka.metrics.group.MetricsCollectorGroup;
import org.dashbuilder.dataprovider.kafka.metrics.group.MetricsCollectorGroupFactory;
import org.dashbuilder.dataprovider.kafka.model.KafkaMetricsRequest;
import org.dashbuilder.dataset.def.KafkaDataSetDef.MetricsTarget;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class KafkaMetricsProviderTest {

    @Mock
    private MetricsCollectorGroupFactory groupsFactory;
    @Mock
    private KafkaMetricsRequest request;
    @Mock
    private MetricsCollectorGroup group;
    @InjectMocks
    private KafkaMetricsProvider provider;

    @Before
    public void setup() {
        when(request.getMetricsTarget()).thenReturn(MetricsTarget.BROKER);

        doReturn(Arrays.asList(mockCollector("abc"),
                               mockCollector("def"),
                               mockCollector("ghi"),
                               mockCollector("adg"))).when(group).getMetricsCollectors(eq(request));

        when(groupsFactory.forTarget(eq(MetricsTarget.BROKER))).thenReturn(group);
    }

    @Test
    public void testCollectorsFor() {
        when(request.filter()).thenReturn(Optional.empty());
        List<KafkaMetricColllector> selectedCollectors = provider.collectorsFor(request);
        assertEquals(4, selectedCollectors.size());
    }

    @Test
    public void testCollectorsForWithFilter() {
        when(request.filter()).thenReturn(Optional.of("a"));
        List<KafkaMetricColllector> selectedCollectors = provider.collectorsFor(request);
        assertEquals(2, selectedCollectors.size());
    }

    @Test
    public void testCollectorsForEmptyFilter() {
        when(request.filter()).thenReturn(Optional.of("  "));
        List<KafkaMetricColllector> selectedCollectors = provider.collectorsFor(request);
        assertEquals(4, selectedCollectors.size());
    }

    @Test
    public void testCollectorsForWithFilterCase() {
        when(request.filter()).thenReturn(Optional.of("AbC"));
        List<KafkaMetricColllector> selectedCollectors = provider.collectorsFor(request);
        assertEquals(1, selectedCollectors.size());
    }

    private KafkaMetricColllector mockCollector(String name) {
        KafkaMetricColllector collector = mock(KafkaMetricColllector.class);
        when(collector.getName()).thenReturn(name);
        return collector;
    }
}