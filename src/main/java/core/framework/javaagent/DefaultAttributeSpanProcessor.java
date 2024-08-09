package core.framework.javaagent;

import io.opentelemetry.context.Context;
import io.opentelemetry.sdk.common.CompletableResultCode;
import io.opentelemetry.sdk.trace.ReadWriteSpan;
import io.opentelemetry.sdk.trace.ReadableSpan;
import io.opentelemetry.sdk.trace.SpanProcessor;

/**
 * @author ebin
 */
public class DefaultAttributeSpanProcessor implements SpanProcessor {
    @Override
    public void onStart(Context parentContext, ReadWriteSpan span) {
        span.setAttribute("error.code", "NONE");
    }

    @Override
    public boolean isStartRequired() {
        return true;
    }

    @Override
    public void onEnd(ReadableSpan span) {
    }

    @Override
    public boolean isEndRequired() {
        return false;
    }

    @Override
    public CompletableResultCode shutdown() {
        return CompletableResultCode.ofSuccess();
    }

    @Override
    public CompletableResultCode forceFlush() {
        return CompletableResultCode.ofSuccess();
    }
}