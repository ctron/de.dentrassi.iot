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

import java.util.function.Consumer;
import java.util.stream.Stream;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.impl.DefaultConsumer;
import org.apache.camel.impl.DefaultMessage;

import de.dentrassi.iot.w1.SensorValue;

public class OneWireConsumer extends DefaultConsumer {

    private final OneWireEndpoint endpoint;

    private final Consumer<Stream<SensorValue>> listener = this::handleUpdate;

    public OneWireConsumer(final OneWireEndpoint endpoint, final Processor processor) {
        super(endpoint, processor);
        this.endpoint = endpoint;
    }

    @Override
    protected void doStart() throws Exception {
        super.doStart();
        this.endpoint.addListener(this.listener);
    }

    @Override
    protected void doStop() throws Exception {
        this.endpoint.removeListener(this.listener);
        super.doStop();
    }

    public void handleUpdate(final Stream<SensorValue> updates) {
        updates.forEach(this::handleUpdate);
    }

    protected void handleUpdate(final SensorValue value) {
        final Exchange exchange = getEndpoint().createExchange();
        final DefaultMessage message = new DefaultMessage();
        message.setBody(value);
        exchange.setIn(message);
        try {
            getAsyncProcessor().process(exchange);
        } catch (final Exception e) {
            this.log.debug("Failed to process message", e);
        }
    }

}
