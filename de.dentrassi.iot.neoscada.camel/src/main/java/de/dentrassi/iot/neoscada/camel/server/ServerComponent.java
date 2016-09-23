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

import java.net.InetSocketAddress;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;

import org.apache.camel.CamelContext;
import org.apache.camel.Endpoint;
import org.apache.camel.impl.UriEndpointComponent;
import org.eclipse.scada.core.ConnectionInformation;
import org.eclipse.scada.da.common.ngp.ProtocolConfigurationFactoryImpl;
import org.eclipse.scada.protocol.ngp.common.ProtocolConfigurationFactory;

import com.google.common.escape.Escaper;
import com.google.common.net.UrlEscapers;

public class ServerComponent extends UriEndpointComponent {

	private int port = 2101;

	private CamelHive hive;
	private HiveRunner runner;

	private ExecutorService executor;

	public ServerComponent() {
		super(ServerEndpoint.class);
	}

	public ServerComponent(final CamelContext context) {
		super(context, ServerEndpoint.class);
	}

	@Override
	protected Endpoint createEndpoint(final String uri, final String remaining, final Map<String, Object> parameters)
			throws Exception {
		return new ServerEndpoint(uri, this, this.hive, remaining);
	}

	@Override
	protected void doStart() throws Exception {
		super.doStart();

		this.executor = getCamelContext().getExecutorServiceManager().newSingleThreadExecutor(this,
				"NeoSCADAComponent");

		this.hive = new CamelHive();
		this.runner = new HiveRunner(this.hive, makeProtocol(), makeAddresses());
	}

	@Override
	protected void doStop() throws Exception {
		if (this.runner != null) {
			this.runner.close();
			this.runner = null;
		}
		if (this.executor != null) {
			getCamelContext().getExecutorServiceManager().shutdown(this.executor);
			this.executor = null;
		}
		super.doStop();
	}

	public ExecutorService getExecutor() {
		return this.executor;
	}

	private ProtocolConfigurationFactory makeProtocol() {
		return new ProtocolConfigurationFactoryImpl(ConnectionInformation.fromURI(makeUri()));
	}

	private final Escaper esc = UrlEscapers.urlFormParameterEscaper();

	private String makeUri() {
		final Map<String, String> parameters = new HashMap<>();

		final StringBuilder sb = new StringBuilder();
		for (final Map.Entry<String, String> entry : parameters.entrySet()) {
			if (sb.length() == 0) {
				sb.append("?");
			} else {
				sb.append("&");
			}

			sb.append(this.esc.escape(entry.getKey()));
			sb.append("=");
			sb.append(this.esc.escape(entry.getValue()));
		}

		return String.format("da:ngp://%s:%s%s", "dummy", this.port, sb);
	}

	private Collection<InetSocketAddress> makeAddresses() {
		return Collections.singletonList(new InetSocketAddress(this.port));
	}

	/**
	 * Set the NGP exporter port
	 */
	public void setPort(final int port) {
		this.port = port;
	}

	public int getPort() {
		return this.port;
	}
}
