package core.framework.javaagent;

import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.trace.SpanKind;
import io.opentelemetry.context.Context;
import io.opentelemetry.sdk.trace.data.LinkData;
import io.opentelemetry.sdk.trace.samplers.Sampler;
import io.opentelemetry.sdk.trace.samplers.SamplingDecision;
import io.opentelemetry.sdk.trace.samplers.SamplingResult;

import java.util.List;

/**
 * @author ebin
 */
public class ErrorLevelSampler implements Sampler {
    private final double ratio;

    public ErrorLevelSampler(double ratio) {
        this.ratio = ratio;
    }

    @Override
    public SamplingResult shouldSample(
        Context parentContext,
        String traceId,
        String name,
        SpanKind spanKind,
        Attributes attributes,
        List<LinkData> parentLinks) {

        String errorLevel = attributes.get(AttributeKey.stringKey("error.level"));
        boolean isNonError = (!"WARN".equals(errorLevel) && !"ERROR".equals(errorLevel));
        if (isNonError) {
            Sampler ratioSampler = Sampler.traceIdRatioBased(ratio);
            return ratioSampler.shouldSample(parentContext, traceId, name, spanKind, attributes, parentLinks);
        } else {
            return SamplingResult.create(SamplingDecision.RECORD_AND_SAMPLE);
        }
    }

    @Override
    public String getDescription() {
        return "ErrorLevelSampler";
    }
}
