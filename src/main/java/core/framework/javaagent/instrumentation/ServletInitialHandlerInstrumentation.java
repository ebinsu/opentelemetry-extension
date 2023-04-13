package core.framework.javaagent.instrumentation;

import io.opentelemetry.api.trace.Span;
import io.opentelemetry.javaagent.extension.instrumentation.TypeInstrumentation;
import io.opentelemetry.javaagent.extension.instrumentation.TypeTransformer;
import io.opentelemetry.javaagent.extension.matcher.AgentElementMatchers;
import io.undertow.security.idm.Account;
import io.undertow.server.HttpServerExchange;
import io.undertow.servlet.handlers.ServletRequestContext;
import io.undertow.servlet.spec.HttpServletRequestImpl;
import io.undertow.util.Headers;
import jakarta.servlet.http.HttpSession;
import net.bytebuddy.asm.Advice;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.matcher.ElementMatcher;
import net.bytebuddy.matcher.ElementMatchers;

import java.util.logging.Logger;

import static net.bytebuddy.matcher.ElementMatchers.namedOneOf;

/**
 * @author ebin
 */
public class ServletInitialHandlerInstrumentation implements TypeInstrumentation {
    private static final Logger logger = Logger.getLogger(ServletInitialHandlerInstrumentation.class.getName());

    @Override
    public ElementMatcher<TypeDescription> typeMatcher() {
        return AgentElementMatchers.hasSuperType(
                namedOneOf("io.undertow.servlet.handlers.ServletInitialHandler"));
    }

    @Override
    public void transform(TypeTransformer transformer) {
        transformer.applyAdviceToMethod(
                namedOneOf("dispatchRequest")
                        .and(
                                ElementMatchers.takesArgument(
                                        0, ElementMatchers.named("io.undertow.server.HttpServerExchange")))
                        .and(
                                ElementMatchers.takesArgument(
                                        1, ElementMatchers.named("io.undertow.servlet.handlers.ServletRequestContext")))
                        .and(
                                ElementMatchers.takesArgument(
                                        2, ElementMatchers.named("io.undertow.servlet.handlers.ServletChain")))
                        .and(
                                ElementMatchers.takesArgument(
                                        3, ElementMatchers.named("jakarta.servlet.DispatcherType")))
                        .and(ElementMatchers.isPrivate()),
                this.getClass().getName() + "$HandleDispatchRequest");
    }

    public static class HandleDispatchRequest {

        @Advice.OnMethodEnter(suppress = Throwable.class)
        public static void onEnter(@Advice.Argument(value = 0) HttpServerExchange exchange, @Advice.Argument(value = 1) ServletRequestContext context) {
            Span current = Span.current();
            HttpServletRequestImpl request = context.getOriginalRequest();
            // host name
            String host = exchange.getHostName();
            if (host == null) {
                host = request.getHeader((Headers.X_FORWARDED_HOST));
            }
            current.setAttribute("host", host);
            // principal id
            HttpSession session = request.getSession(false);
            if (session != null) {
                Account account = (Account) session.getAttribute("undertow_account");
                current.setAttribute("principal.id", account.getPrincipal().getName());
            }
        }
    }
}
