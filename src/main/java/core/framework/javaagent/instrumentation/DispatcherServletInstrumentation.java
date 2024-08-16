/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package core.framework.javaagent.instrumentation;

import io.opentelemetry.api.trace.Span;
import io.opentelemetry.javaagent.extension.instrumentation.TypeInstrumentation;
import io.opentelemetry.javaagent.extension.instrumentation.TypeTransformer;
import io.opentelemetry.javaagent.extension.matcher.AgentElementMatchers;
import jakarta.servlet.http.HttpServletRequest;
import net.bytebuddy.asm.Advice;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.matcher.ElementMatcher;
import net.bytebuddy.matcher.ElementMatchers;

import java.security.Principal;
import java.util.Objects;

import static net.bytebuddy.matcher.ElementMatchers.namedOneOf;

/**
 * @author ebin
 */
public class DispatcherServletInstrumentation implements TypeInstrumentation {
    @Override
    public ElementMatcher<TypeDescription> typeMatcher() {
        return AgentElementMatchers.hasSuperType(
            namedOneOf("org.springframework.web.servlet.DispatcherServlet"));
    }

    @Override
    public void transform(TypeTransformer typeTransformer) {
        typeTransformer.applyAdviceToMethod(
            namedOneOf("doDispatch")
                .and(
                    ElementMatchers.takesArgument(
                        0, ElementMatchers.named("jakarta.servlet.http.HttpServletRequest")))
                .and(
                    ElementMatchers.takesArgument(
                        1, ElementMatchers.named("jakarta.servlet.http.HttpServletResponse")))
                .and(ElementMatchers.isProtected()),
            this.getClass().getName() + "$DoDispatchAdvice");
    }

    @SuppressWarnings("unused")
    public static class DoDispatchAdvice {

        @Advice.OnMethodEnter(suppress = Throwable.class)
        public static void onMethodEnter(@Advice.Argument(value = 0) HttpServletRequest request) {
            Span current = Span.current();
            if (Objects.nonNull(current)) {
                Principal userPrincipal = request.getUserPrincipal();
                if (userPrincipal != null) {
                    current.setAttribute("principal.name", userPrincipal.getName());
                }
            }
        }
    }
}