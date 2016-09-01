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

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

import org.apache.camel.Component;
import org.apache.camel.Consumer;
import org.apache.camel.Processor;
import org.apache.camel.Producer;
import org.apache.camel.impl.DefaultEndpoint;
import org.apache.camel.spi.Metadata;
import org.apache.camel.spi.UriEndpoint;
import org.apache.camel.spi.UriPath;

import de.dentrassi.iot.opentsdb.collector.Collector;
import de.dentrassi.iot.opentsdb.collector.CollectorBuilder;
import de.dentrassi.iot.opentsdb.collector.string.StringOptions;

@UriEndpoint(scheme = "open-tsdb", syntax = "open-tsdb:uri?option1=value1&option2=value2", title = "OpenTSDB", label = "iot")
public class OpenTsdbEndpoint extends DefaultEndpoint {

    /**
     * Target server URL
     */
    @UriPath
    @Metadata(required = "true")
    private final String url;

    private final Map<String, Object> options;
    private Collector collector;

    public OpenTsdbEndpoint(final String endpointUri, final Component component, final String remaining,
            final Map<String, Object> parameters) throws MalformedURLException {
        super(endpointUri, component);
        this.url = remaining;
        this.options = parameters;
    }

    @Override
    protected void doStart() throws Exception {
        final URI uri = URI.create(this.url);

        final String fragment = uri.getFragment();
        final String baseUrl;
        if (fragment != null) {
            baseUrl = this.url.replaceFirst(Pattern.quote("#" + fragment) + "$", "");
        } else {
            baseUrl = uri.toString();
        }

        final CollectorBuilder builder = new CollectorBuilder(new URL(baseUrl));

        final Map<String, String> stringOptions = new HashMap<>(this.options.size());
        for (final Map.Entry<String, ?> entry : this.options.entrySet()) {
            final String key = entry.getKey();
            final Object value = entry.getValue();
            stringOptions.put(key, value != null ? value.toString() : null);
        }

        StringOptions.putAll(builder, stringOptions);

        this.collector = builder.build().orElseThrow(() -> new IllegalArgumentException("Failed to create collector"));
    }

    @Override
    protected void doStop() throws Exception {
        if (this.collector != null) {
            this.collector.close();
            this.collector = null;
        }
        super.doStop();
    }

    @Override
    public Producer createProducer() throws Exception {
        String metric = null;
        Map<String, String> tags = null;

        final String fragment = URI.create(this.url).getFragment(); // #metric/foo=bar/
        if (fragment != null) {
            final String[] toks = fragment.split("/");
            if (toks.length > 0) {
                metric = toks[0];
                tags = new HashMap<>();
                for (int i = 1; i < toks.length; i++) {
                    final String tagAndValue = toks[i];
                    final String[] tav = tagAndValue.split("=", 2);
                    if (tav.length == 2) {
                        tags.put(tav[0], tav[1]);
                    }
                }
            }
        }

        return new OpenTsdbProducer(this, this.collector, metric, tags);
    }

    @Override
    public Consumer createConsumer(final Processor processor) throws Exception {
        return null;
    }

    @Override
    public boolean isSingleton() {
        return false;
    }

}
