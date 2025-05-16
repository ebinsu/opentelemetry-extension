package core.framework.javaagent.instrumentation;

import io.opentelemetry.api.GlobalOpenTelemetry;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.SpanKind;
import io.opentelemetry.context.Context;
import io.opentelemetry.context.Scope;

import java.util.concurrent.Callable;

import static core.framework.javaagent.instrumentation.CoreFrameworkInstrumentationModule.INSTRUMENTATION_NAME;

/**
 * @author ebin
 */
public final class ContextAwareCallable<T> implements Callable<T> {
    private final Callable<T> delegate;
    private final Context context;

    public ContextAwareCallable(Callable<T> delegate, Context context) {
        this.delegate = delegate;
        this.context = context;
    }

    public static <T> Callable<T> wrap(Callable<T> delegate, Context context) {
        return new ContextAwareCallable<>(delegate, context);
    }

    @Override
    public T call() throws Exception {
        try (Scope scope = context.makeCurrent()) {
            Span span = GlobalOpenTelemetry.getTracer(INSTRUMENTATION_NAME)
                .spanBuilder(Thread.currentThread().getName())
                .setSpanKind(SpanKind.INTERNAL)
                .startSpan();
            try (Scope innerScope = span.makeCurrent()) {
                return delegate.call();
            } catch (Throwable t) {
                span.recordException(t);
                throw t;
            } finally {
                span.end();
            }
        }
    }
}
