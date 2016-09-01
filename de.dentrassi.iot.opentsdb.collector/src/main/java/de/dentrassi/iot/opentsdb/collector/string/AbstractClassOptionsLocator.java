package de.dentrassi.iot.opentsdb.collector.string;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import de.dentrassi.iot.opentsdb.collector.CollectorBuilder.Option;

public class AbstractClassOptionsLocator implements OptionsLocator {

    private final Map<String, Option<?>> map = new HashMap<>();

    public AbstractClassOptionsLocator(final Class<?>... clazzes) {
        for (final Class<?> clazz : clazzes) {
            addClass(clazz);
        }
    }

    private void addClass(final Class<?> clazz) {
        for (final Field field : clazz.getFields()) {
            if (!Modifier.isStatic(field.getModifiers())) {
                continue;
            }

            final Class<?> optType = field.getType();
            if (!Option.class.isAssignableFrom(optType)) {
                continue;
            }

            final String name = mapName(field.getName());

            try {
                this.map.put(name, (Option<?>) field.get(null));
            } catch (IllegalArgumentException | IllegalAccessException e) {
                // ignore field
            }
        }
    }

    @Override
    public Map<String, Option<?>> getOptions() {
        return Collections.unmodifiableMap(this.map);
    }

    private static String mapName(final String name) {
        final StringBuilder sb = new StringBuilder(name.length());

        boolean toUpper = false;
        for (final char c : name.toCharArray()) {
            switch (c) {
            case '_':
                toUpper = true;
                break;
            default:
                if (!toUpper) {
                    sb.append(Character.toLowerCase(c));
                } else {
                    sb.append(Character.toUpperCase(c));
                    toUpper = false;
                }

                break;
            }
        }

        return sb.toString();
    }
}
