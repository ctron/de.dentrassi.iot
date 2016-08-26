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

package de.dentrassi.iot.w1.io;

import static java.nio.file.Files.readAllLines;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import de.dentrassi.iot.w1.Sensor;

public class Scanner {

    private static final Pattern SLAVE_NAME_PATTERN = Pattern.compile("\\p{XDigit}{2}-\\p{XDigit}{12}");

    private final class ResultEntry implements Map.Entry<Sensor, ReadResult<RawSensorValue>> {
        private final Sensor sensor;
        private ReadResult<RawSensorValue> result;

        private ResultEntry(final Sensor sensor, final ReadResult<RawSensorValue> result) {
            this.sensor = sensor;
            this.result = result;
        }

        @Override
        public Sensor getKey() {
            return this.sensor;
        }

        @Override
        public ReadResult<RawSensorValue> getValue() {
            return this.result;
        }

        @Override
        public ReadResult<RawSensorValue> setValue(final ReadResult<RawSensorValue> value) {
            final ReadResult<RawSensorValue> oldResult = this.result;
            this.result = value;
            return oldResult;
        }
    }

    private final Path base;

    public Scanner(final Path base) {
        this.base = base;
    }

    public Scanner() {
        this(Paths.get("/sys/bus/w1/devices"));
    }

    public RawSensorValue rawRead(final Sensor source) throws IOException {
        final List<String> lines = readAllLines(dataSensorPath(source), StandardCharsets.UTF_8);

        return new RawSensorValue(source, lines);
    }

    public ReadResult<RawSensorValue> read(final Sensor source) {
        try {
            final List<String> lines = readAllLines(dataSensorPath(source), StandardCharsets.UTF_8);
            return new ReadResult<>(new RawSensorValue(source, lines));
        } catch (final IOException e) {
            return new ReadResult<>(e);
        }
    }

    public Stream<String> scanMasters() {
        try {
            return Files.list(this.base) //
                    .filter(p -> Files.isDirectory(p)) // only directories
                    .map(p -> p.getFileName().toString()) //
                    .filter(n -> n.startsWith("w1_bus_master"));
        } catch (final IOException e) {
            return Stream.empty();
        }
    }

    public Stream<Sensor> scanSensors(final String master) {
        final Path masterBase = this.base.resolve(master);
        try {
            return Files.list(masterBase) //
                    .filter(Files::isDirectory) // only directories
                    .map(p -> p.getFileName().toString()) // map to file name
                    .filter(n -> SLAVE_NAME_PATTERN.matcher(n).matches()) // only matching entries
                    .map(n -> new Sensor(master, n));
        } catch (final IOException e) {
            return Stream.empty();
        }
    }

    public Stream<Sensor> scanAllSensors() {
        return scanMasters().flatMap(this::scanSensors);
    }

    public Stream<Map.Entry<Sensor, ReadResult<RawSensorValue>>> readAllSensors() {
        return scanAllSensors().map(this::readEntry);
    }

    private Map.Entry<Sensor, ReadResult<RawSensorValue>> readEntry(final Sensor sensor) {
        final ReadResult<RawSensorValue> result = read(sensor);
        return new ResultEntry(sensor, result);
    }

    private Path baseSensorPath(final Sensor source) {
        return this.base.resolve(source.getMaster()).resolve(source.getSlave());
    }

    private Path dataSensorPath(final Sensor source) {
        return baseSensorPath(source).resolve("w1_slave");
    }
}
