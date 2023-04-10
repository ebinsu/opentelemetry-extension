/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package core.framework.javaagent.instrumentation;

import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.StatusCode;
import io.opentelemetry.javaagent.extension.instrumentation.TypeInstrumentation;
import io.opentelemetry.javaagent.extension.instrumentation.TypeTransformer;
import io.opentelemetry.javaagent.extension.matcher.AgentElementMatchers;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import net.bytebuddy.asm.Advice;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.matcher.ElementMatcher;
import net.bytebuddy.matcher.ElementMatchers;

import java.util.Objects;
import java.util.Optional;
import java.util.logging.Logger;

import static net.bytebuddy.matcher.ElementMatchers.namedOneOf;

/**
 * @author ebin
 */
public class DispatcherServletInstrumentation implements TypeInstrumentation {
    private static final Logger logger = Logger.getLogger(DispatcherServletInstrumentation.class.getName());

    @Override
    public ElementMatcher<TypeDescription> typeMatcher() {
        return AgentElementMatchers.hasSuperType(
                namedOneOf("org.springframework.web.servlet.DispatcherServlet"));
    }

    @Override
    public void transform(TypeTransformer typeTransformer) {
        typeTransformer.applyAdviceToMethod(
                namedOneOf("processDispatchResult")
                        .and(
                                ElementMatchers.takesArgument(
                                        0, ElementMatchers.named("jakarta.servlet.http.HttpServletRequest")))
                        .and(
                                ElementMatchers.takesArgument(
                                        1, ElementMatchers.named("jakarta.servlet.http.HttpServletResponse")))
                        .and(
                                ElementMatchers.takesArgument(
                                        2,
                                        ElementMatchers.named("org.springframework.web.servlet.HandlerExecutionChain")))
                        .and(
                                ElementMatchers.takesArgument(
                                        3, ElementMatchers.named("org.springframework.web.servlet.ModelAndView")))
                        .and(ElementMatchers.takesArgument(4, ElementMatchers.named("java.lang.Exception")))
                        .and(ElementMatchers.isPrivate()),
                this.getClass().getName() + "$ProcessDispatchResultAdvice");
    }

    @SuppressWarnings("unused")
    public static class ProcessDispatchResultAdvice {

        @Advice.OnMethodExit(suppress = Throwable.class)
        public static void onExit(
                @Advice.Argument(value = 0) HttpServletRequest request,
                @Advice.Argument(value = 1) HttpServletResponse httpServletResponse,
                @Advice.Argument(value = 4) Exception exception) {
            Span current = Span.current();
            if (Objects.nonNull(exception)) {
                String defaultErrorCode = "UNASSIGNED";
                String errorCode = (String) request.getAttribute("core.framework.web.exception.DefaultHandlerExceptionResolver.ERROR.CODE");
                current.setStatus(StatusCode.ERROR, Optional.ofNullable(errorCode).orElse(defaultErrorCode));
                current.setAttribute("error.code", Optional.ofNullable(errorCode).orElse(defaultErrorCode));
            } else {
                current.setStatus(StatusCode.OK);
                current.setAttribute("error.code", "NONE");
            }
        }
    }
}
