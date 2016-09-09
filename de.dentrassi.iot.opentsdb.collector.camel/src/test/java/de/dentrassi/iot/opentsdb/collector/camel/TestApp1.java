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

import org.apache.camel.CamelContext;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.impl.DefaultCamelContext;

public class TestApp1 {
    public static void main(final String[] args) throws Exception {
        final CamelContext context = new DefaultCamelContext();

        // add routes

        context.addRoutes(new RouteBuilder() {

            @Override
            public void configure() throws Exception {
                from("paho:sensors/test2/temperature?brokerUrl=tcp://iot.eclipse.org").log("${body}") //
                        .convertBodyTo(String.class).convertBodyTo(Float.class) //
                        .to("open-tsdb:http://localhost:4242#test2/value=temp");

                from("paho:tele/devices/TEMP?brokerUrl=tcp://iot.eclipse.org").log("${body}") //
                        .convertBodyTo(String.class).convertBodyTo(Float.class) //
                        .to("open-tsdb:http://localhost:4242#test3/value=temp");
            }
        });

        // start

        context.start();

        // sleep

        while (true) {
            Thread.sleep(Long.MAX_VALUE);
        }
    }
}
