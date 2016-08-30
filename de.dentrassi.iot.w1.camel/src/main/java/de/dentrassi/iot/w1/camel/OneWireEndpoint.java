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

package de.dentrassi.iot.w1.camel;

import java.net.URI;
import java.nio.file.Paths;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.stream.Stream;

import org.apache.camel.Component;
import org.apache.camel.Processor;
import org.apache.camel.Producer;
import org.apache.camel.impl.DefaultEndpoint;
import org.apache.camel.spi.Metadata;
import org.apache.camel.spi.UriEndpoint;
import org.apache.camel.spi.UriPath;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.dentrassi.iot.w1.SensorValue;
import de.dentrassi.iot.w1.io.Scanner;
import de.dentrassi.iot.w1.parser.ValueParser;
import de.dentrassi.iot.w1.polling.SensorPoller;

@UriEndpoint(scheme = "w1", syntax = "w1:*", title = "One Wire", label = "iot", consumerClass = OneWireConsumer.class)
public class OneWireEndpoint extends DefaultEndpoint {

    private static final Logger logger = LoggerFactory.getLogger(OneWireEndpoint.class);

    /**
     * The URI path
     */
    @UriPath
    @Metadata(required = "true")
    private final String remaining;

    private final Set<Consumer<Stream<SensorValue>>> listeners = new CopyOnWriteArraySet<>();
    private Scanner scanner;
    private ValueParser parser;
    private SensorPoller poller;

    public OneWireEndpoint(final String endpointUri, final String remaining, final Component camelContext) {
        super(endpointUri, camelContext);
        logger.debug("New endpoint: {}", remaining);
        this.remaining = remaining;
    }

    @Override
    public Producer createProducer() throws Exception {
        return null;
    }

    @Override
    public OneWireConsumer createConsumer(final Processor processor) throws Exception {
        return new OneWireConsumer(this, processor);
    }

    @Override
    public boolean isSingleton() {
        return true;
    }

    @Override
    protected void doStart() throws Exception {
        super.doStart();
        if (this.remaining == null || "*".equals(this.remaining)) {
            this.scanner = new Scanner();
        } else if (this.remaining.startsWith("/")) {
            this.scanner = new Scanner(Paths.get(URI.create("file:" + this.remaining)));
        } else {
            this.scanner = new Scanner(Paths.get(URI.create(this.remaining)));
        }
        this.parser = new ValueParser();
        this.poller = new SensorPoller(this.scanner, this.parser, 1, TimeUnit.SECONDS, this::handleUpdate);
    }

    @Override
    protected void doStop() throws Exception {
        if (this.poller == null) {
            this.poller.close();
            this.poller = null;
        }
        this.parser = null;
        this.scanner = null;
        super.doStop();
    }

    protected void handleUpdate(final Stream<SensorValue> value) {
        for (final Consumer<Stream<SensorValue>> listener : this.listeners) {
            try {
                listener.accept(value);
            } catch (final Exception e) {
                logger.warn("Failed to process update for listener");
            }
        }
    }

    void addListener(final Consumer<Stream<SensorValue>> listener) {
        this.listeners.add(listener);
    }

    void removeListener(final Consumer<Stream<SensorValue>> listener) {
        this.listeners.remove(listener);
    }
}
