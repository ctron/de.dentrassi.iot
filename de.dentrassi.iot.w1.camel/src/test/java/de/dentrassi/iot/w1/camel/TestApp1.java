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

package de.dentrassi.iot.w1.camel;

import java.util.Arrays;
import java.util.LinkedList;

import org.apache.camel.CamelContext;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.impl.DefaultCamelContext;

public class TestApp1 {
    public static void main(final String[] args) throws Exception {

        final LinkedList<String> a = new LinkedList<>(Arrays.asList(args));

        String target = a.pollFirst();
        if (target == null) {
            target = "*";
        }

        String mqttBroker = a.pollFirst();
        if (mqttBroker == null) {
            mqttBroker = "tcp://iot.eclipse.org:1883";
        }

        runMain("w1:" + target, mqttBroker);
    }

    private static void runMain(final String uri, final String mqttBroker) throws Exception, InterruptedException {
        final CamelContext context = new DefaultCamelContext();

        // add routes

        context.addRoutes(new RouteBuilder() {

            @Override
            public void configure() throws Exception {
                from(uri).log("${body.value}") //
                        .setBody(simple("${body.value}")) //
                        .convertBodyTo(String.class).convertBodyTo(byte[].class) //
                        .to("paho:sensors/test2/temperature?brokerUrl=" + mqttBroker);
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
