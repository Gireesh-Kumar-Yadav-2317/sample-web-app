package com.onemoretechie.sample.config;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tag;
import io.micrometer.core.instrument.config.MeterFilter;
import java.time.Clock;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.actuate.autoconfigure.metrics.MeterRegistryCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ObservabilityConfig {

    @Bean
    public Clock systemUtcClock() {
        return Clock.systemUTC();
    }

    @Bean
    public MeterRegistryCustomizer<MeterRegistry> meterRegistryCustomizer(
            @Value("${spring.application.name:sample-web-app}") String app) {
        return registry -> registry.config()
                .meterFilter(MeterFilter.commonTags(List.of(Tag.of("application", app))))
                .meterFilter(MeterFilter.deny(id -> {
                    String uri = id.getTag("uri");
                    return uri != null && (uri.startsWith("/actuator") || uri.equals("/favicon.ico"));
                }));
    }
}
