/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package core.framework.javaagent;

import com.google.auto.service.AutoService;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.trace.SpanKind;
import io.opentelemetry.context.Context;
import io.opentelemetry.sdk.autoconfigure.spi.AutoConfigurationCustomizer;
import io.opentelemetry.sdk.autoconfigure.spi.AutoConfigurationCustomizerProvider;
import io.opentelemetry.sdk.autoconfigure.spi.ConfigProperties;
import io.opentelemetry.sdk.trace.SdkTracerProviderBuilder;
import io.opentelemetry.sdk.trace.data.LinkData;
import io.opentelemetry.sdk.trace.samplers.Sampler;
import io.opentelemetry.sdk.trace.samplers.SamplingDecision;
import io.opentelemetry.sdk.trace.samplers.SamplingResult;

import java.util.List;

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
                double ratio = configProperties.getDouble("otel.traces.sampler.ratio", 0.5);

                Sampler healthSampler = new HealthEndpointSampler(endpoint);
                Sampler errorLevelSampler = new ErrorLevelSampler(ratio);

                return new Sampler() {
                    @Override
                    public SamplingResult shouldSample(Context parentContext, String traceId, String name, SpanKind spanKind, Attributes attributes, List<LinkData> parentLinks) {
                        if (healthSampler.shouldSample(parentContext, traceId, name, spanKind, attributes, parentLinks).getDecision() == SamplingDecision.DROP) {
                            return SamplingResult.drop();
                        }
                        return errorLevelSampler.shouldSample(parentContext, traceId, name, spanKind, attributes, parentLinks);
                    }

                    @Override
                    public String getDescription() {
                        return "CompositeSampler";
                    }
                };

            })
            .addTracerProviderCustomizer(this::configureSdkTracerProvider);
    }

    private SdkTracerProviderBuilder configureSdkTracerProvider(
        SdkTracerProviderBuilder tracerProvider, ConfigProperties config) {
        return tracerProvider.addSpanProcessor(new DefaultAttributeSpanProcessor());
    }
}
