package core.framework.javaagent.instrumentation;

import io.opentelemetry.api.trace.Span;
import io.opentelemetry.javaagent.extension.instrumentation.TypeInstrumentation;
import io.opentelemetry.javaagent.extension.instrumentation.TypeTransformer;
import io.opentelemetry.javaagent.extension.matcher.AgentElementMatchers;
import io.undertow.server.HttpServerExchange;
import net.bytebuddy.asm.Advice;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.matcher.ElementMatcher;
import net.bytebuddy.matcher.ElementMatchers;

import java.util.Objects;

import static net.bytebuddy.matcher.ElementMatchers.namedOneOf;

/**
 * @author ebin
 */
public class ServletInitialHandlerInstrumentation implements TypeInstrumentation {
    @Override
    public ElementMatcher<TypeDescription> typeMatcher() {
        return AgentElementMatchers.hasSuperType(
                namedOneOf("io.undertow.servlet.handlers.ServletInitialHandler"));
    }

    @Override
    public void transform(TypeTransformer transformer) {
        transformer.applyAdviceToMethod(
                namedOneOf("handleRequest")
                        .and(
                                ElementMatchers.takesArgument(
                                        0, ElementMatchers.named("io.undertow.server.HttpServerExchange")))
                        .and(ElementMatchers.isPublic()),
                this.getClass().getName() + "$HandleRequestAdvice");
    }

    public static class HandleRequestAdvice {

        @Advice.OnMethodEnter(suppress = Throwable.class)
        public static void onEnter(@Advice.Argument(value = 0) HttpServerExchange exchange) {
//            Span current = Span.current();
//            current.setAttribute("test", "hahahah1");
//            if (Objects.nonNull(current)) {
//                current.setAttribute("test", "hahahah2");
//                current.updateName(exchange.getRequestMethod() + " " + exchange.getRequestURI());
//            }
        }
    }
}
