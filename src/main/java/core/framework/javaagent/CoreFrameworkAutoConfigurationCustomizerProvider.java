/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package core.framework.javaagent;

import com.google.auto.service.AutoService;
import io.opentelemetry.sdk.autoconfigure.spi.AutoConfigurationCustomizer;
import io.opentelemetry.sdk.autoconfigure.spi.AutoConfigurationCustomizerProvider;
import io.opentelemetry.sdk.autoconfigure.spi.ConfigProperties;
import io.opentelemetry.sdk.trace.SdkTracerProviderBuilder;

/**
 * This is one of the main entry points for Instrumentation Agent's customizations. It allows
 * configuring the {@link AutoConfigurationCustomizer}. See the {@link
 * #customize(AutoConfigurationCustomizer)} method below.
 *
 * <p>Also see https://github.com/open-telemetry/opentelemetry-java/issues/2022
 *
 * @see AutoConfigurationCustomizerProvider
 */
@AutoService(AutoConfigurationCustomizerProvider.class)
public class CoreFrameworkAutoConfigurationCustomizerProvider implements AutoConfigurationCustomizerProvider {

    @Override
    public void customize(AutoConfigurationCustomizer autoConfiguration) {
        autoConfiguration
            .addSamplerCustomizer((sampler, configProperties) -> {
                String endpoint = configProperties.getString("otel.traces.sampler.health.endpoint", "health");
                return new HealthEndpointSampler(endpoint);
            })
            .addTracerProviderCustomizer(this::configureSdkTracerProvider);
    }

    private SdkTracerProviderBuilder configureSdkTracerProvider(
        SdkTracerProviderBuilder tracerProvider, ConfigProperties config) {
        return tracerProvider.addSpanProcessor(new DefaultAttributeSpanProcessor());
    }
}
