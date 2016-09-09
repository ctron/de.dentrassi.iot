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

package de.dentrassi.iot.opentsdb.collector;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * The collector interface
 * <p>
 * Use the {@link CollectorBuilder} to create a new instance.
 * </p>
 * <p>
 * The following example shows how to publish some data: <code><pre>
CollectorBuilder builder = new CollectorBuilder(new URL("http://localhost:4242"));
builder.option(SYNC, true);

try (Collector collector = builder.build()
       .orElseThrow(() -> new IllegalStateException("Failed to get collector"))) {

    CompletableFuture<Void> result = collector
       .publish(new Data("random", r.nextFloat() * 100.0f, now(), singletonMap("test", "foo")));

    result.join();
}
 * </pre></code>
 * </p>
 */
public interface Collector extends AutoCloseable {
    public default CompletableFuture<Void> publish(final Data data) {
        return publish(Collections.singletonList(data));
    }

    public default CompletableFuture<Void> publish(final Data[] data) {
        return publish(Arrays.asList(data));
    }

    public CompletableFuture<Void> publish(List<Data> data);
}
