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

package de.dentrassi.iot.w1.app;

import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

import de.dentrassi.iot.w1.SensorValue;
import de.dentrassi.iot.w1.io.Scanner;
import de.dentrassi.iot.w1.parser.ValueParser;
import de.dentrassi.iot.w1.polling.SensorPoller;

public class Poller {
    public static void main(final String[] args) throws InterruptedException {
        final Scanner scanner = new Scanner();
        final ValueParser parser = new ValueParser();

        try (SensorPoller poller = new SensorPoller(scanner, parser, 1, TimeUnit.SECONDS, Poller::dump)) {
            for (;;) {
                Thread.sleep(Long.MAX_VALUE);
            }
        }
    }

    private static void dump(final Stream<SensorValue> updates) {
        System.out.println("==== Update ====");
        updates.forEach(System.out::println);
    }
}
