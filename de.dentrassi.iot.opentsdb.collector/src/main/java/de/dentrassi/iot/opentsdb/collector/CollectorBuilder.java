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

import java.net.URL;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.ServiceLoader;
import java.util.function.Consumer;
import java.util.function.Function;

import de.dentrassi.iot.opentsdb.collector.service.CollectorFactory;

/**
 * The collector builder
 */
public class CollectorBuilder {

    public static interface Option<T> {

        public T cast(Object value);

        public T fromString(final String value);

        public default void put(final CollectorBuilder builder, final String value) {
            builder.option(this, fromString(value));
        }

        public default void apply(final Map<?, ?> options, final Consumer<T> consumer) {
            final Object value = options.get(this);
            consumer.accept(cast(value));
        }

        public default void applyNonNull(final Map<?, ?> options, final Consumer<T> consumer) {
            final Object value = options.get(this);
            if (value == null) {
                return;
            }

            consumer.accept(cast(value));
        }

        public default void applyNonNullOrElse(final Map<?, ?> options, final T defaultValue,
                final Consumer<T> consumer) {
            final Object value = options.get(this);
            if (value != null) {
                consumer.accept(cast(value));
            } else {
                consumer.accept(defaultValue);
            }
        }

        public default T orElse(final Map<?, ?> options, final T defaultValue) {
            final Object value = options.get(this);
            if (value == null) {
                return defaultValue;
            }
            return cast(value);
        }

        public default T get(final Map<?, ?> options) {
            return cast(options.get(this));
        }
    }

    public static abstract class AbstractOption<T> implements Option<T> {
        private final Class<T> clazz;

        public AbstractOption(final Class<T> clazz) {
            Objects.requireNonNull(clazz);
            this.clazz = clazz;
        }

        @Override
        public T cast(final Object value) {
            return this.clazz.cast(value);
        }
    }

    public static class StringOption extends AbstractOption<String> {
        public StringOption() {
            super(String.class);
        }

        @Override
        public String fromString(final String value) {
            return value;
        }
    }

    public static class FunctionOption<T> extends AbstractOption<T> {
        private final Function<String, T> func;

        public FunctionOption(final Class<T> clazz, final Function<String, T> func) {
            super(clazz);
            Objects.requireNonNull(func);
            this.func = func;
        }

        @Override
        public T fromString(final String value) {
            return this.func.apply(value);
        }
    }

    public static class BooleanOption extends FunctionOption<Boolean> {
        public BooleanOption() {
            super(Boolean.class, Boolean::parseBoolean);
        }
    }

    public static class IntegerOption extends FunctionOption<Integer> {
        public IntegerOption() {
            super(Integer.class, Integer::parseInt);
        }
    }

    public static class LongOption extends FunctionOption<Long> {
        public LongOption() {
            super(Long.class, Long::parseLong);
        }
    }

    public static final class StandardOptions {
        public static final Option<String> BACKEND = new StringOption();
        public static final Option<Boolean> MILLISECONDS = new BooleanOption();
    }

    public static final class Request {
        private ClassLoader classLoader;

        private URL url;

        private final Map<Object, Object> options = new HashMap<>();

        private Request() {
        }

        public ClassLoader getClassLoader() {
            if (this.classLoader == null) {
                return Thread.currentThread().getContextClassLoader();
            } else {
                return this.classLoader;
            }
        }

        public URL getUrl() {
            return this.url;
        }

        public Map<?, ?> getOptions() {
            return Collections.unmodifiableMap(this.options);
        }

        public String getBackend() {
            return StandardOptions.BACKEND.get(this.options);
        }
    }

    private final Request request = new Request();

    public CollectorBuilder(final URL url) {
        this.request.url = url;
        defaultBackend();
    }

    public <T> CollectorBuilder option(final Option<T> option, final T value) {
        Objects.requireNonNull(option);
        this.request.options.put(option, value);
        return this;
    }

    public CollectorBuilder removeOption(final Option<?> option) {
        Objects.requireNonNull(option);
        this.request.options.remove(option);
        return this;
    }

    public CollectorBuilder usingClassLoader(final ClassLoader classLoader) {
        this.request.classLoader = classLoader;
        return this;
    }

    public CollectorBuilder contextClassLoader() {
        return usingClassLoader(null);
    }

    public CollectorBuilder backend(final String backendName) {
        return option(StandardOptions.BACKEND, backendName);
    }

    public CollectorBuilder defaultBackend() {
        return removeOption(StandardOptions.BACKEND);
    }

    public CollectorBuilder httpBackend() {
        return backend("http");
    }

    public CollectorBuilder enableMilliseconds() {
        return milliseconds(true);
    }

    public CollectorBuilder milliseconds(final boolean milliseconds) {
        return option(StandardOptions.MILLISECONDS, milliseconds);
    }

    public Request getRequest() {
        return this.request;
    }

    public Optional<Collector> build() {
        // try with service loader

        final ServiceLoader<CollectorFactory> loader = ServiceLoader.load(CollectorFactory.class,
                this.request.getClassLoader());
        return Optional.ofNullable(tryWith(this.request, loader.iterator()));
    }

    private Collector tryWith(final Request request, final Iterator<CollectorFactory> iterator) {
        while (iterator.hasNext()) {
            final CollectorFactory factory = iterator.next();
            if (factory == null) {
                continue;
            }

            if (!factory.canBuild(request)) {
                continue;
            }

            return factory.build(request);
        }

        return null;
    }
}
