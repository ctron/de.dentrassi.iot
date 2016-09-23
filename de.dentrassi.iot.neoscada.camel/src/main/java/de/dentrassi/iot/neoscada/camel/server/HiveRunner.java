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
import java.util.LinkedList;

import org.eclipse.scada.da.core.server.Hive;
import org.eclipse.scada.da.server.ngp.Exporter;
import org.eclipse.scada.protocol.ngp.common.ProtocolConfigurationFactory;

public class HiveRunner implements AutoCloseable {
	private final Hive hive;
	private final Exporter exporter;

	public HiveRunner(final Hive hive, final ProtocolConfigurationFactory protocolConfigurationFactory,
			final Collection<InetSocketAddress> addresses) throws Exception {

		this.hive = hive;
		this.hive.start();

		this.exporter = new Exporter(hive, protocolConfigurationFactory, addresses);
		try {
			this.exporter.start();
		} catch (final Exception e) {
			hive.stop();
			throw e;
		}
	}

	@Override
	public void close() throws Exception {
		final LinkedList<Exception> errors = new LinkedList<>();

		if (this.hive != null) {
			try {
				this.hive.stop();
			} catch (final Exception e) {
				errors.add(e);
			}
		}
		if (this.exporter != null) {
			try {
				this.exporter.stop();
			} catch (final Exception e) {
				errors.add(e);
			}
		}

		if (!errors.isEmpty()) {
			final Exception e = errors.pollFirst();
			errors.stream().forEach(e::addSuppressed);
			throw e;
		}
	}
}
