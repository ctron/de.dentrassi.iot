package de.dentrassi.iot.opentsdb.collector.string;

import java.util.Map;

import de.dentrassi.iot.opentsdb.collector.CollectorBuilder.Option;

public interface OptionsLocator {
    public Map<String, Option<?>> getOptions();
}
