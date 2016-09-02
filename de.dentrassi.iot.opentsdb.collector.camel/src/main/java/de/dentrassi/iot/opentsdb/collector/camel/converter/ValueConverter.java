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

package de.dentrassi.iot.opentsdb.collector.camel.converter;

import java.time.Instant;

import org.apache.camel.Converter;
import org.apache.camel.TypeConverters;

import de.dentrassi.iot.opentsdb.collector.camel.Value;

@Converter
public final class ValueConverter implements TypeConverters {

    private ValueConverter() {
    }

    @Converter
    public static Value fromLong(final Long value) {
        return new Value(value, Instant.now());
    }

    @Converter
    public static Value fromLong(final long value) {
        return new Value(value, Instant.now());
    }

    @Converter
    public static Value fromInt(final Integer value) {
        return new Value(value, Instant.now());
    }

    @Converter
    public static Value fromInt(final int value) {
        return new Value(value, Instant.now());
    }

    @Converter
    public static Value fromFloat(final Float value) {
        return new Value(value, Instant.now());
    }

    @Converter
    public static Value fromFloat(final float value) {
        return new Value(value, Instant.now());
    }
}
