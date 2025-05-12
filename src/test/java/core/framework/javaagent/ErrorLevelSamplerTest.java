package core.framework.javaagent;

import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.trace.SpanKind;
import io.opentelemetry.context.Context;
import io.opentelemetry.sdk.trace.IdGenerator;
import io.opentelemetry.sdk.trace.samplers.SamplingDecision;
import io.opentelemetry.sdk.trace.samplers.SamplingResult;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Collections;

/**
 * @author ebin
 */
public class ErrorLevelSamplerTest {
    private ErrorLevelSampler sampler;
    private final Context parentContext = Context.root();

    @BeforeEach
    void setup() {
        sampler = new ErrorLevelSampler(0.5);
    }

    @Test
    void testErrorLevelError() {
        Attributes attributes = Attributes.of(AttributeKey.stringKey("error.level"), "ERROR");
        SamplingResult result = sampler.shouldSample(
            parentContext, "traceId", "spanName", SpanKind.SERVER, attributes, Collections.emptyList()
        );
        Assertions.assertEquals(SamplingDecision.RECORD_AND_SAMPLE, result.getDecision());
    }

    @Test
    void testErrorLevelInfo() {
        Attributes attributes = Attributes.of(AttributeKey.stringKey("error.level"), "INFO");
        int sampledCount = 0;
        for (int i = 0; i < 1000; i++) {
            SamplingResult result = sampler.shouldSample(
                parentContext, IdGenerator.random().generateTraceId(), "spanName", SpanKind.SERVER, attributes, Collections.emptyList()
            );
            if (result.getDecision() == SamplingDecision.RECORD_AND_SAMPLE) {
                sampledCount++;
            }
        }
        Assertions.assertTrue(sampledCount > 400 && sampledCount < 600);
    }

    @Test
    void testMissingErrorLevel() {
        Attributes attributes = Attributes.empty();
        int sampledCount = 0;
        for (int i = 0; i < 1000; i++) {
            SamplingResult result = sampler.shouldSample(
                parentContext, IdGenerator.random().generateTraceId(), "spanName", SpanKind.SERVER, attributes, Collections.emptyList()
            );
            if (result.getDecision() == SamplingDecision.RECORD_AND_SAMPLE) {
                sampledCount++;
            }
        }
        Assertions.assertTrue(sampledCount > 400 && sampledCount < 600);
    }

}
