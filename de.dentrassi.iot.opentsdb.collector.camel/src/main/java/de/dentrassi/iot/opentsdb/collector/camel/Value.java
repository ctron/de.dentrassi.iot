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

package de.dentrassi.iot.opentsdb.collector.camel;

import java.time.Instant;

public class Value {
    private final Number value;
    private final Instant timestamp;

    public Value(final float value, final Instant timestamp) {
        this.value = value;
        this.timestamp = timestamp;
    }

    public Value(final long value, final Instant timestamp) {
        this.value = value;
        this.timestamp = timestamp;
    }

    public Number getValue() {
        return this.value;
    }

    public Instant getTimestamp() {
        return this.timestamp;
    }

}
