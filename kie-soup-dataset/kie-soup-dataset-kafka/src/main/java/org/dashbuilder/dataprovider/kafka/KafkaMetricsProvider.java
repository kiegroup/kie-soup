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

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import javax.management.MBeanServerConnection;
import javax.management.remote.JMXConnector;

import org.dashbuilder.dataprovider.kafka.mbean.MBeanServerConnectionProvider;
import org.dashbuilder.dataprovider.kafka.metrics.KafkaMetricColllector;
import org.dashbuilder.dataprovider.kafka.metrics.group.MetricsCollectorGroupFactory;
import org.dashbuilder.dataprovider.kafka.model.KafkaMetric;
import org.dashbuilder.dataprovider.kafka.model.KafkaMetricsRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provides the metrics for a given Kafka metrics request.
 *
 */
public class KafkaMetricsProvider {

    protected Logger LOGGER = LoggerFactory.getLogger(KafkaMetricsProvider.class);

    private static KafkaMetricsProvider INSTANCE;

    MetricsCollectorGroupFactory metricsCollectorGroupFactory;

    static {
        MetricsCollectorGroupFactory metricsCollectorGroupFactory = MetricsCollectorGroupFactory.get();
        INSTANCE = new KafkaMetricsProvider(metricsCollectorGroupFactory);
    }

    KafkaMetricsProvider(MetricsCollectorGroupFactory metricsCollectorGroupFactory) {
        this.metricsCollectorGroupFactory = metricsCollectorGroupFactory;
    }

    public static KafkaMetricsProvider get() {
        return INSTANCE;
    }

    public List<KafkaMetric> getMetrics(KafkaMetricsRequest request) {
        List<KafkaMetricColllector> extractors = collectorsFor(request);
        JMXConnector connector = MBeanServerConnectionProvider.newConnection(request);
        try {
            MBeanServerConnection mbsc = connector.getMBeanServerConnection();
            return extractMetrics(mbsc, extractors, request);
        } catch (Exception e) {
            LOGGER.warn("Error reading metrics for request {}", request);
            LOGGER.debug("Error reading metrics for request", e);
            return Collections.emptyList();
        } finally {
            try {
                connector.close();
            } catch (IOException e) {
                LOGGER.warn("Error closing JMX connector");
                LOGGER.debug("Error closing JMX Connector", e);
            }
        }
    }

    List<KafkaMetricColllector> collectorsFor(KafkaMetricsRequest request) {
        List<KafkaMetricColllector> collectors = metricsCollectorGroupFactory.forTarget(request.getMetricsTarget())
                                                                             .getMetricsCollectors(request);
        return request.filter()
                      .map(f -> filtering(collectors, f))
                      .orElse(collectors);
    }

    List<KafkaMetricColllector> filtering(List<KafkaMetricColllector> collectors, String filter) {
        return filter.trim().isEmpty() ? collectors : collectors.stream()
                                                                .filter(c -> c.getName().toLowerCase().contains(filter.toLowerCase()))
                                                                .collect(Collectors.toList());
    }

    private List<KafkaMetric> extractMetrics(MBeanServerConnection mbsc, List<KafkaMetricColllector> extractors, KafkaMetricsRequest request) {
        return extractors.stream()
                         .flatMap(e -> e.collect(mbsc).stream())
                         .collect(Collectors.toList());
    }

}
