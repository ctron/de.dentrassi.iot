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
import org.apache.camel.Message;
import org.apache.camel.Processor;
import org.apache.camel.impl.DefaultConsumer;
import org.apache.camel.impl.DefaultMessage;
import org.eclipse.scada.core.Variant;
import org.eclipse.scada.core.server.OperationParameters;

public class ServerConsumer extends DefaultConsumer {

	public ServerConsumer(final ServerEndpoint endpoint, final Processor processor) {
		super(endpoint, processor);
	}

	public void processWrite(final Variant value, final OperationParameters operationParameters) throws Exception {
		final Exchange exchange = getEndpoint().createExchange();
		exchange.setIn(makeMessage(value, operationParameters));

		getProcessor().process(exchange);
	}

	private Message makeMessage(final Variant value, final OperationParameters operationParameters) {
		final DefaultMessage message = new DefaultMessage();

		message.setBody(value);
		message.setHeader("operationParameters", operationParameters);

		return message;
	}
}
