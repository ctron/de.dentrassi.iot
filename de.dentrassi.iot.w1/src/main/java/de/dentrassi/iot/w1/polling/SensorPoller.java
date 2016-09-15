/*
 * Copyright (C) 2016 Jens Reimann <jreimann@redhat.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package de.dentrassi.iot.w1.polling;

import java.time.Instant;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.dentrassi.iot.w1.ErrorSensorValue;
import de.dentrassi.iot.w1.Sensor;
import de.dentrassi.iot.w1.SensorValue;
import de.dentrassi.iot.w1.io.Scanner;
import de.dentrassi.iot.w1.parser.ValueParser;

public class SensorPoller implements AutoCloseable {
    private static final Logger logger = LoggerFactory.getLogger(SensorPoller.class);

    private static final AtomicLong COUNTER = new AtomicLong();
    private final ScheduledExecutorService executor;
    private final Scanner scanner;
    private final ValueParser parser;
    private Map<Sensor, SensorValue> last = Collections.emptyMap();
    private final Consumer<Stream<SensorValue>> consumer;

    public SensorPoller(final Scanner scanner, final ValueParser parser, final long rate, final TimeUnit unit,
            final Consumer<Stream<SensorValue>> consumer) {
        this.executor = createExecutor();
        this.executor.scheduleAtFixedRate(this::update, 0, rate, unit);
        this.scanner = scanner;
        this.parser = parser;
        this.consumer = consumer;
    }

    @Override
    public void close() throws InterruptedException {
        this.executor.shutdown();
        this.executor.awaitTermination(Long.MAX_VALUE, TimeUnit.MILLISECONDS);
    }

    protected void update() {
        final Map<Sensor, SensorValue> found = new HashMap<>();

        try (final Stream<SensorValue> values = this.scanner.readAllSensors().flatMap(this.parser::parse)) {
            values.forEach(value -> {
                found.put(value.getSensor(), value);
            });
        } catch (final Exception e) {
            logger.warn("Failed to update", e);
        }

        final Set<Sensor> removed = new HashSet<>(this.last.keySet());
        removed.removeAll(found.keySet());

        this.last = found;

        try (final Stream<SensorValue> stream = Stream.concat(found.values().stream(),
                removed.stream().map(SensorPoller::toError))) {
            handleUpdate(stream);
        }
    }

    protected void handleUpdate(final Stream<SensorValue> updateStream) {
        this.consumer.accept(updateStream);
    }

    private static SensorValue toError(final Sensor sensor) {
        return new ErrorSensorValue(sensor, new IllegalStateException("Sensor lost"), Instant.now());
    }

    private static ScheduledExecutorService createExecutor() {
        return Executors.newSingleThreadScheduledExecutor(new ThreadFactory() {

            @Override
            public Thread newThread(final Runnable r) {
                final Thread t = new Thread(r);
                t.setName("SensorPoller/" + COUNTER.incrementAndGet());
                return t;
            }
        });
    }
}
