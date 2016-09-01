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

package de.dentrassi.iot.opentsdb.collector;

import java.time.Instant;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class Data {
    private final String metric;
    private final Number value;
    private final Instant timestamp;
    private final Map<String, String> tags;

    public Data(final String metric, final long value, final Instant timestamp, final Map<String, String> tags) {
        this(metric, (Number) value, timestamp, tags);
    }

    public Data(final String metric, final float value, final Instant timestamp, final Map<String, String> tags) {
        this(metric, (Number) value, timestamp, tags);
    }

    private Data(final String metric, final Number value, final Instant timestamp, final Map<String, String> tags) {
        Objects.requireNonNull(metric);
        Objects.requireNonNull(value);
        Objects.requireNonNull(timestamp);
        Objects.requireNonNull(tags);

        this.metric = metric;
        this.value = value;
        this.timestamp = timestamp;
        this.tags = Collections.unmodifiableMap(new HashMap<>(tags));
    }

    public String getMetric() {
        return this.metric;
    }

    public Number getValue() {
        return this.value;
    }

    public Instant getTimestamp() {
        return this.timestamp;
    }

    public Map<String, String> getTags() {
        return this.tags;
    }
}
