package de.dentrassi.iot.opentsdb.collector.http;

import static de.dentrassi.iot.opentsdb.collector.http.HttpCollector.Options.SYNC;
import static java.time.Instant.now;
import static java.util.Collections.singletonMap;

import java.net.URL;
import java.util.Random;
import java.util.concurrent.CompletableFuture;

import de.dentrassi.iot.opentsdb.collector.Collector;
import de.dentrassi.iot.opentsdb.collector.CollectorBuilder;
import de.dentrassi.iot.opentsdb.collector.Data;

public class TestApp1 {
    public static void main(final String[] args) throws Exception {

        final Random r = new Random();

        final CollectorBuilder builder = new CollectorBuilder(new URL("http://localhost:4242"));
        builder.option(SYNC, true);

        try (final Collector collector = builder.build()
                .orElseThrow(() -> new IllegalStateException("Failed to get collector"))) {

            final CompletableFuture<Void> result = collector
                    .publish(new Data("random", r.nextFloat() * 100.0f, now(), singletonMap("test", "foo")));

            result.join();
        }
    }
}
