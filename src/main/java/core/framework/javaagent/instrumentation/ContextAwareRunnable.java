package core.framework.javaagent.instrumentation;

import io.opentelemetry.api.GlobalOpenTelemetry;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.SpanKind;
import io.opentelemetry.context.Context;
import io.opentelemetry.context.Scope;

import static core.framework.javaagent.instrumentation.CoreFrameworkInstrumentationModule.INSTRUMENTATION_NAME;

/**
 * @author ebin
 */
public final class ContextAwareRunnable implements Runnable {
    private final Runnable delegate;
    private final Context context;

    public ContextAwareRunnable(Runnable delegate, Context context) {
        this.delegate = delegate;
        this.context = context;
    }

    @Override
    public void run() {
        try (Scope scope = context.makeCurrent()) {
            Span span = GlobalOpenTelemetry.getTracer(INSTRUMENTATION_NAME)
                .spanBuilder(Thread.currentThread().getName())
                .setSpanKind(SpanKind.INTERNAL)
                .startSpan();
            try (Scope innerScope = span.makeCurrent()) {
                delegate.run();
            } catch (Throwable t) {
                span.recordException(t);
                throw t;
            } finally {
                span.end();
            }
        }
    }

    public static Runnable wrap(Runnable delegate, Context context) {
        return new ContextAwareRunnable(delegate, context);
    }
}
