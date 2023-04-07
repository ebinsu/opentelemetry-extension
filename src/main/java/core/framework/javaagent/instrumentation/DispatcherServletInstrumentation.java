/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package core.framework.javaagent.instrumentation;

import core.framework.exception.BaseRuntimeException;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.StatusCode;
import io.opentelemetry.javaagent.extension.instrumentation.TypeInstrumentation;
import io.opentelemetry.javaagent.extension.instrumentation.TypeTransformer;
import io.opentelemetry.javaagent.extension.matcher.AgentElementMatchers;
import net.bytebuddy.asm.Advice;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.matcher.ElementMatcher;
import net.bytebuddy.matcher.ElementMatchers;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Objects;
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
                                        0, ElementMatchers.named("javax.servlet.http.HttpServletRequest")))
                        .and(
                                ElementMatchers.takesArgument(
                                        1, ElementMatchers.named("javax.servlet.http.HttpServletResponse")))
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

        @Advice.OnMethodEnter(suppress = Throwable.class)
        public static void onEnter(
                @Advice.Argument(value = 0) HttpServletRequest request,
                @Advice.Argument(value = 1) HttpServletResponse httpServletResponse,
                @Advice.Argument(value = 4) Exception exception) {
            Span current = Span.current();
            if (Objects.nonNull(exception)) {
                String errorCode = "UNASSIGNED";
                if (exception instanceof BaseRuntimeException) {
                    BaseRuntimeException e = (BaseRuntimeException) exception;
                }
                current.setStatus(StatusCode.ERROR, errorCode);
                current.setAttribute("error.code", errorCode);
            } else {
                current.setStatus(StatusCode.OK);
                current.setAttribute("error.code", "OK");
            }
        }
    }
}
