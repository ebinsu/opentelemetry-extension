package core.framework.javaagent;

import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.trace.SpanKind;
import io.opentelemetry.context.Context;
import io.opentelemetry.sdk.trace.IdGenerator;
import io.opentelemetry.sdk.trace.data.LinkData;
import io.opentelemetry.sdk.trace.samplers.Sampler;
import io.opentelemetry.sdk.trace.samplers.SamplingDecision;
import io.opentelemetry.sdk.trace.samplers.SamplingResult;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;

/**
 * @author ebin
 */
public class CompositeSamplerTest {
    private final Context parentContext = Context.root();

    @Test
    public void test() {
        Sampler healthSampler = new HealthEndpointSampler("health");
        Sampler errorLevelSampler = new ErrorLevelSampler(0.5);

        Sampler sampler = new Sampler() {
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

        Attributes attributes = Attributes.of(AttributeKey.stringKey("http.target"), "/health");
        SamplingResult result = sampler.shouldSample(parentContext, IdGenerator.random().generateTraceId(), "spanName", SpanKind.SERVER, attributes, Collections.emptyList());
        Assertions.assertSame(SamplingDecision.DROP, result.getDecision());

        attributes = Attributes.of(AttributeKey.stringKey("error.level"), "ERROR");
        result = sampler.shouldSample(parentContext, IdGenerator.random().generateTraceId(), "spanName", SpanKind.SERVER, attributes, Collections.emptyList());
        Assertions.assertSame(SamplingDecision.RECORD_AND_SAMPLE, result.getDecision());

        attributes = Attributes.of(AttributeKey.stringKey("http.target"), "/health", AttributeKey.stringKey("error.level"), "ERROR");
        result = sampler.shouldSample(parentContext, IdGenerator.random().generateTraceId(), "spanName", SpanKind.SERVER, attributes, Collections.emptyList());
        Assertions.assertSame(SamplingDecision.DROP, result.getDecision());

        attributes = Attributes.of(AttributeKey.stringKey("http.target"), "/health", AttributeKey.stringKey("error.level"), "INFO");
        result = sampler.shouldSample(parentContext, IdGenerator.random().generateTraceId(), "spanName", SpanKind.SERVER, attributes, Collections.emptyList());
        Assertions.assertSame(SamplingDecision.DROP, result.getDecision());

        attributes = Attributes.of(AttributeKey.stringKey("http.target"), "/test", AttributeKey.stringKey("error.level"), "ERROR");
        result = sampler.shouldSample(parentContext, IdGenerator.random().generateTraceId(), "spanName", SpanKind.SERVER, attributes, Collections.emptyList());
        Assertions.assertSame(SamplingDecision.RECORD_AND_SAMPLE, result.getDecision());
    }
}
