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

package de.dentrassi.iot.w1.parser;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

import de.dentrassi.iot.w1.ErrorSensorValue;
import de.dentrassi.iot.w1.Sensor;
import de.dentrassi.iot.w1.SensorValue;
import de.dentrassi.iot.w1.io.RawSensorValue;
import de.dentrassi.iot.w1.io.ReadResult;

public class ValueParser {

    public static final List<LineParser> PARSERS;

    static {
        final List<LineParser> parsers = new ArrayList<>();
        parsers.add(new TemperatureParser());
        PARSERS = Collections.unmodifiableList(parsers);
    }

    private final List<LineParser> parsers;

    public ValueParser() {
        this(PARSERS);
    }

    public ValueParser(final List<LineParser> parsers) {
        this.parsers = parsers;
    }

    public Stream<SensorValue> parse(final Map.Entry<Sensor, ReadResult<RawSensorValue>> entry) {
        return parse(entry.getKey(), entry.getValue());
    }

    public Stream<SensorValue> parse(final Sensor sensor, final ReadResult<RawSensorValue> result) {
        final Instant timestamp = result.getTimestamp();

        if (result.getError() != null) {
            return Stream.of(new ErrorSensorValue(sensor, result.getError(), timestamp));
        }

        final List<String> lines = result.getData().getData();

        final List<SensorValue> list = new LinkedList<>();
        for (final LineParser parser : this.parsers) {
            try {
                for (final String line : lines) {
                    final Optional<SensorValue> parsed = parser.parse(sensor, timestamp, line);
                    parsed.ifPresent(list::add);
                }
            } catch (final Exception e) {
                // parser failed
            }
        }

        return list.stream();
    }
}
