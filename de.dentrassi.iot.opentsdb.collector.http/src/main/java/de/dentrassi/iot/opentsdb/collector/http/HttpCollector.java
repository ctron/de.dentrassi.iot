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

import static de.dentrassi.iot.opentsdb.collector.http.HttpCollector.Options.CONNECTION_TIMEOUT;
import static de.dentrassi.iot.opentsdb.collector.http.HttpCollector.Options.SYNC;
import static java.util.concurrent.TimeUnit.MILLISECONDS;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import com.google.gson.GsonBuilder;

import de.dentrassi.iot.opentsdb.collector.Collector;
import de.dentrassi.iot.opentsdb.collector.CollectorBuilder.BooleanOption;
import de.dentrassi.iot.opentsdb.collector.CollectorBuilder.LongOption;
import de.dentrassi.iot.opentsdb.collector.CollectorBuilder.Option;
import de.dentrassi.iot.opentsdb.collector.CollectorBuilder.StandardOptions;
import de.dentrassi.iot.opentsdb.collector.Data;
import de.dentrassi.iot.opentsdb.collector.PublishException;
import de.dentrassi.iot.opentsdb.collector.http.gson.DataTypeAdapter;
import de.dentrassi.iot.opentsdb.collector.http.gson.PutResponse;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.HttpUrl;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.OkHttpClient.Builder;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class HttpCollector implements Collector {

    public static final class Options {
        final static Option<Long> CONNECTION_TIMEOUT = new LongOption();

        final static Option<Boolean> SYNC = new BooleanOption();
    }

    private static final MediaType MEDIA_TYPE = MediaType.parse("text/json");

    private final OkHttpClient client;
    private final String putUrl;

    private final GsonBuilder gsonBuilder;

    private final boolean sync;

    public HttpCollector(final String baseUrl, final Map<?, ?> options) {
        this.putUrl = baseUrl + "/api/put";

        final Builder builder = new OkHttpClient.Builder();
        customizeBuilder(options, builder);
        this.client = builder.build();

        this.gsonBuilder = new GsonBuilder();
        this.gsonBuilder.registerTypeAdapter(Data.class,
                new DataTypeAdapter(StandardOptions.MILLISECONDS.orElse(options, false)));
        this.sync = SYNC.orElse(options, false);
    }

    protected void customizeBuilder(final Map<?, ?> options, final Builder builder) {
        CONNECTION_TIMEOUT.applyNonNull(options, timeout -> builder.connectTimeout(timeout, MILLISECONDS));
    }

    @Override
    public void close() throws IOException {
        if (this.client.dispatcher() != null && this.client.dispatcher().executorService() != null) {
            this.client.dispatcher().executorService().shutdown();
        }
        if (this.client.connectionPool() != null) {
            this.client.connectionPool().evictAll();
        }
        if (this.client.cache() != null) {
            this.client.cache().close();
        }
    }

    @Override
    public CompletableFuture<Void> publish(final List<Data> data) {
        if (data.isEmpty()) {
            return CompletableFuture.completedFuture(null);
        }

        final Request.Builder rb = new Request.Builder();

        final HttpUrl.Builder url = HttpUrl.parse(this.putUrl).newBuilder();
        if (this.sync) {
            url.addQueryParameter("sync", null);
        }
        rb.url(url.build());

        rb.post(RequestBody.create(MEDIA_TYPE, this.gsonBuilder.create().toJson(data)));

        final CompletableFuture<Void> result = new CompletableFuture<>();

        final Call call = this.client.newCall(rb.build());
        call.enqueue(new Callback() {

            @Override
            public void onResponse(final Call call, final Response response) throws IOException {
                try {
                    handleResponse(result, response);
                } finally {
                    response.close();
                }
            }

            @Override
            public void onFailure(final Call call, final IOException e) {
                result.completeExceptionally(e);
            }
        });

        return result;
    }

    private void handleResponse(final CompletableFuture<Void> result, final Response response) {
        try {

            switch (response.code()) {
            case 204:
                result.complete(null);
                return;
            case 400: {
                handleBody(result, response);
                result.complete(null);
                return;
            }
            default:
                result.completeExceptionally(new PublishException(
                        String.format("Unknown response code - %s: %s", response.code(), response.body().string())));
                return;
            }
        } catch (final Exception e) {
            result.completeExceptionally(e);
        }
    }

    private void handleBody(final CompletableFuture<Void> result, final Response response) {
        final PutResponse pr = this.gsonBuilder.create().fromJson(response.body().charStream(), PutResponse.class);

        if (pr == null) {
            result.completeExceptionally(new PublishException("Failed to read response body"));
            return;
        }

        if (pr.getFailed() > 0) {
            result.completeExceptionally(new PublishException(
                    String.format("Failed to publish data - OK: %s, failed: %s", pr.getSuccess(), pr.getFailed())));
            return;
        }
    }

}
