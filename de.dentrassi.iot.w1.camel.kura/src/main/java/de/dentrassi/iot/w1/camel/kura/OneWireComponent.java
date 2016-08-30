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

package de.dentrassi.iot.w1.camel.kura;

import java.util.Map;

import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.eclipse.kura.camel.router.CamelRouter;
import org.eclipse.kura.configuration.ConfigurableComponent;
import org.eclipse.kura.message.KuraPayload;
import org.osgi.framework.Constants;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ConfigurationPolicy;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Modified;

import de.dentrassi.iot.w1.FloatSensorValue;
import de.dentrassi.iot.w1.SensorValue;

@Component(property = { Constants.SERVICE_PID
        + "=de.dentrassi.iot.w1.camel.kura.OneWireComponent" }, enabled = true, immediate = true, configurationPolicy = ConfigurationPolicy.REQUIRE)
public class OneWireComponent extends CamelRouter implements ConfigurableComponent {

    @Override
    protected void beforeStart(CamelContext camelContext) {
        super.beforeStart(camelContext);
    }

    @Override
    public void configure() throws Exception {
        from("w1:*").process(this::process).to("kura-cloud:w1/metrics");
    }

    @Activate
    @Override
    protected void activate(ComponentContext componentContext, Map<String, Object> properties) {
        super.activate(componentContext, properties);
    }

    @Deactivate
    @Override
    protected void deactivate(ComponentContext componentContext) {
        super.deactivate(componentContext);
    }

    @Modified
    @Override
    protected void modified(Map<String, Object> properties) {
        super.modified(properties);
    }

    private void process(final Exchange exchange) {
        final SensorValue value = exchange.getIn().getBody(SensorValue.class);

        if (value instanceof FloatSensorValue) {
            final FloatSensorValue floatValue = (FloatSensorValue) value;
            final KuraPayload kp = new KuraPayload();
            kp.addMetric(value.getSensor().getMasterAndSlave(), floatValue.getValue());

        }
    }
}
