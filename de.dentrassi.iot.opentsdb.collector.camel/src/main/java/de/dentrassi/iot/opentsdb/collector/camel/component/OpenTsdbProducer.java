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

package de.dentrassi.iot.opentsdb.collector.camel.component;

import java.util.Map;

import org.apache.camel.AsyncCallback;
import org.apache.camel.Endpoint;
import org.apache.camel.Exchange;
import org.apache.camel.impl.DefaultAsyncProducer;

import de.dentrassi.iot.opentsdb.collector.Collector;
import de.dentrassi.iot.opentsdb.collector.Data;
import de.dentrassi.iot.opentsdb.collector.camel.Value;

public class OpenTsdbProducer extends DefaultAsyncProducer {

    private final Collector collector;
    private final String metric;
    private final Map<String, String> tags;

    public OpenTsdbProducer(final Endpoint endpoint, final Collector collector, final String metric,
            final Map<String, String> tags) {
        super(endpoint);
        this.collector = collector;
        this.metric = metric;
        this.tags = tags;
    }

    private void publish(final Data[] data, final Exchange exchange, final AsyncCallback callback) {
        this.collector.publish(data).whenComplete((result, e) -> {
            if (e != null) {
                exchange.setException(e);
            }
            callback.done(false);
        });
    }

    @SuppressWarnings("unchecked")
    @Override
    public boolean process(final Exchange exchange, final AsyncCallback callback) {
        {
            final Data[] datas = exchange.getIn().getBody(Data[].class);
            if (datas != null) {
                publish(datas, exchange, callback);
                return false;
            }
        }

        {
            final Data data = exchange.getIn().getBody(Data.class);
            if (data != null) {
                publish(new Data[] { data }, exchange, callback);
                return false;
            }
        }

        String metric = this.metric;
        Map<String, String> tags = this.tags;

        if (exchange.getIn().getHeader("metric") != null) {
            metric = exchange.getIn().getHeader("metric").toString();
        }
        if (exchange.getIn().getHeader("tags") != null) {
            tags = (Map<String, String>) exchange.getIn().getHeader("tags");
        }

        Value[] values = exchange.getIn().getBody(Value[].class);
        if (values == null) {
            final Value value = exchange.getIn().getBody(Value.class);
            if (value != null) {
                values = new Value[] { value };
            }
        }

        if (values != null) {
            if (metric == null) {
                exchange.setException(
                        new IllegalArgumentException("Unable to handle Value message type without metric"));
                callback.done(true);
                return true;
            }
            if (tags == null || tags.isEmpty()) {
                exchange.setException(new IllegalArgumentException("Unable to handle Value message type without tags"));
                callback.done(true);
                return true;
            }

            final Data[] data = new Data[values.length];
            for (int i = 0; i < data.length; i++) {
                final Value value = values[i];
                if (value.getValue() instanceof Long) {
                    data[i] = new Data(this.metric, value.getValue().longValue(), value.getTimestamp(), tags);
                } else {
                    data[i] = new Data(this.metric, value.getValue().floatValue(), value.getTimestamp(), tags);
                }
            }
            publish(data, exchange, callback);
            return false;
        }

        exchange.setException(new IllegalArgumentException(String.format("Unable to handle body of '%s'",
                exchange.getIn().getBody() != null ? exchange.getIn().getBody().getClass() : "null")));
        callback.done(true);
        return true;
    }

}
