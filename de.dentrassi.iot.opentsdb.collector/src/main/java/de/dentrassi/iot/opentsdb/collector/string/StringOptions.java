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

package de.dentrassi.iot.opentsdb.collector.string;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.ServiceLoader;
import java.util.Set;

import de.dentrassi.iot.opentsdb.collector.CollectorBuilder;
import de.dentrassi.iot.opentsdb.collector.CollectorBuilder.Option;
import de.dentrassi.iot.opentsdb.collector.CollectorBuilder.StandardOptions;

public class StringOptions {

    private static final Set<OptionsLocator> STANDARD_LOCATORS;

    private final CollectorBuilder builder;

    private Collection<OptionsLocator> services;

    private final Map<String, Option<?>> cache = new HashMap<>();

    static {
        final Set<OptionsLocator> locators = new HashSet<>();
        locators.add(new AbstractClassOptionsLocator(StandardOptions.class));
        STANDARD_LOCATORS = Collections.unmodifiableSet(locators);
    }

    public StringOptions(final CollectorBuilder builder) {
        Objects.requireNonNull(builder);
        this.builder = builder;
    }

    public Set<String> put(final Map<String, String> options) {
        Objects.requireNonNull(options);

        final Set<String> result = new HashSet<>();

        for (final Map.Entry<String, String> entry : options.entrySet()) {
            final Optional<Option<?>> option = lookup(entry.getKey());
            if (option.isPresent()) {
                option.get().put(this.builder, entry.getValue());
            } else {
                result.add(entry.getKey());
            }
        }

        return result;
    }

    private Optional<Option<?>> lookup(final String key) {
        if (this.cache.containsKey(key)) {
            return Optional.ofNullable(this.cache.get(key));
        }
        Option<?> result = from(key, STANDARD_LOCATORS);

        if (result == null) {
            result = from(key, getServices());
        }

        this.cache.put(key, result);

        return Optional.ofNullable(result);
    }

    private Collection<OptionsLocator> getServices() {
        if (this.services == null) {
            return this.services;
        }

        this.services = new LinkedList<>();
        final ServiceLoader<OptionsLocator> services = ServiceLoader.load(OptionsLocator.class,
                this.builder.getRequest().getClassLoader());
        services.forEach(this.services::add);

        return this.services;
    }

    private Option<?> from(final String key, final Collection<OptionsLocator> locators) {
        for (final OptionsLocator locator : locators) {
            if (locator.getOptions().containsKey(key)) {
                return locator.getOptions().get(key);
            }
        }

        return null;
    }

    public static Set<String> putAll(final CollectorBuilder builder, final Map<String, String> options) {
        return new StringOptions(builder).put(options);
    }
}
