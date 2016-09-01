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

package de.dentrassi.iot.opentsdb.collector.http;

import de.dentrassi.iot.opentsdb.collector.Collector;
import de.dentrassi.iot.opentsdb.collector.CollectorBuilder.Request;
import de.dentrassi.iot.opentsdb.collector.service.CollectorFactory;

public class HttpCollectorFactory implements CollectorFactory {

    @Override
    public boolean canBuild(final Request builder) {
        if (builder.getBackend() == null || builder.getBackend().equalsIgnoreCase("http")) {
            return true;
        }
        return false;
    }

    @Override
    public Collector build(final Request builder) {
        return new HttpCollector(builder.getUrl().toString(), builder.getOptions());
    }

}
