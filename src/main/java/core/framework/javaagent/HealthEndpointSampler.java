/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package core.framework.javaagent;

import io.opentelemetry.api.common.AttributeType;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.internal.InternalAttributeKeyImpl;
import io.opentelemetry.api.trace.SpanKind;
import io.opentelemetry.context.Context;
import io.opentelemetry.sdk.trace.data.LinkData;
import io.opentelemetry.sdk.trace.samplers.Sampler;
import io.opentelemetry.sdk.trace.samplers.SamplingDecision;
import io.opentelemetry.sdk.trace.samplers.SamplingResult;

import java.util.List;

/**
 * <p>See <a
 * href="https://github.com/open-telemetry/opentelemetry-specification/blob/master/specification/trace/sdk.md#sampling">
 * OpenTelemetry Specification</a> for more information about span sampling.
 *
 * @see CoreFrameworkAutoConfigurationCustomizerProvider
 */
public class HealthEndpointSampler implements Sampler {
    private String healthEndpoint;

    public HealthEndpointSampler(String healthEndpoint) {
        this.healthEndpoint = healthEndpoint;
    }

    @Override
    public SamplingResult shouldSample(
        Context parentContext,
        String traceId,
        String name,
        SpanKind spanKind,
        Attributes attributes,
        List<LinkData> parentLinks) {
        String attr = attributes.get(InternalAttributeKeyImpl.create("http.target", AttributeType.STRING));
        if (name.contains(healthEndpoint)) {
            return SamplingResult.create(SamplingDecision.DROP);
        } else if (spanKind == SpanKind.INTERNAL && name.contains("OperationHandler.handle")) {
            return SamplingResult.create(SamplingDecision.DROP);
        } else if (spanKind == SpanKind.SERVER && (attr != null && attr.contains(healthEndpoint))) {
            return SamplingResult.create(SamplingDecision.DROP);
        } else {
            return SamplingResult.create(SamplingDecision.RECORD_AND_SAMPLE);
        }
    }

    @Override
    public String getDescription() {
        return "HealthEndpoint";
    }
}
