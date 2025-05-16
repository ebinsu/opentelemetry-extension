package core.framework.javaagent.instrumentation;

import io.opentelemetry.context.Context;
import io.opentelemetry.javaagent.extension.instrumentation.TypeInstrumentation;
import io.opentelemetry.javaagent.extension.instrumentation.TypeTransformer;
import io.opentelemetry.javaagent.extension.matcher.AgentElementMatchers;
import net.bytebuddy.asm.Advice;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.matcher.ElementMatcher;
import net.bytebuddy.matcher.ElementMatchers;

import java.util.concurrent.Callable;

import static net.bytebuddy.matcher.ElementMatchers.namedOneOf;

/**
 * @author ebin
 */
public class ExecutorInstrumentation implements TypeInstrumentation {

    @Override
    public ElementMatcher<TypeDescription> typeMatcher() {
        return AgentElementMatchers.hasSuperType(namedOneOf("core.framework.kernel.async.Executor"));
    }

    @Override
    public void transform(TypeTransformer transformer) {
        transformer.applyAdviceToMethod(
            namedOneOf("submit")
                .and(
                    ElementMatchers.takesArgument(
                        0, ElementMatchers.named("java.lang.String"))
                ).and(
                    ElementMatchers.takesArgument(
                        1, ElementMatchers.named("java.lang.Runnable"))
                )
                .and(ElementMatchers.isPublic()),
            ExecutorInstrumentation.class.getName() + "$WrapRunnable");

        transformer.applyAdviceToMethod(
            namedOneOf("submit")
                .and(
                    ElementMatchers.takesArgument(
                        0, ElementMatchers.named("java.lang.String"))
                ).and(
                    ElementMatchers.takesArgument(
                        1, ElementMatchers.named("java.util.concurrent.Callable"))
                )
                .and(ElementMatchers.isPublic()),
            ExecutorInstrumentation.class.getName() + "$WrapCallable");
    }

    @SuppressWarnings("unused")
    public static class WrapRunnable {
        @Advice.OnMethodEnter(suppress = Throwable.class)
        public static void onEnter(@Advice.Argument(value = 0) String action,
                                   @Advice.Argument(value = 1, readOnly = false) Runnable runnable) {
            Context context = Context.current();
            runnable = ContextAwareRunnable.wrap(runnable, context);
        }
    }

    @SuppressWarnings("unused")
    public static class WrapCallable {
        @Advice.OnMethodEnter(suppress = Throwable.class)
        public static void onEnter(@Advice.Argument(value = 0) String action,
                                   @Advice.Argument(value = 1, readOnly = false) Callable<?> callable) {
            Context context = Context.current();
            callable = ContextAwareCallable.wrap(callable, context);
        }
    }
}
