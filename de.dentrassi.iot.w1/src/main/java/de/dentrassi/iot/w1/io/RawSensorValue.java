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

import java.util.List;

import de.dentrassi.iot.w1.Sensor;

public class RawSensorValue {
    private final Sensor sensor;
    private final List<String> data;

    public RawSensorValue(final Sensor sensor, final List<String> data) {
        this.sensor = sensor;
        this.data = data;
    }

    public Sensor getSensor() {
        return this.sensor;
    }

    public List<String> getData() {
        return this.data;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        for (final String line : this.data) {
            sb.append(line).append(System.lineSeparator());
        }
        return sb.toString();
    }
}