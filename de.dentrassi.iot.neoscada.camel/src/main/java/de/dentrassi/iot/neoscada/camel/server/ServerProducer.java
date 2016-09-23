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

package de.dentrassi.iot.neoscada.camel.server;

import org.apache.camel.Exchange;
import org.apache.camel.impl.DefaultProducer;
import org.eclipse.scada.core.Variant;
import org.eclipse.scada.da.server.common.chain.DataItemInputOutputChained;

public class ServerProducer extends DefaultProducer {

	private final DataItemInputOutputChained item;

	public ServerProducer(final ServerEndpoint endpoint, final DataItemInputOutputChained item) {
		super(endpoint);
		this.item = item;
	}

	@Override
	public void process(final Exchange exchange) throws Exception {
		final Object value = exchange.getIn().getBody();
		this.item.updateData(Variant.valueOf(value), null, null);
	}

}
