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
import net.bytebuddy.asm.Advice;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.matcher.ElementMatcher;
import net.bytebuddy.matcher.ElementMatchers;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Marker;

import java.util.Objects;

import static net.bytebuddy.matcher.ElementMatchers.nameMatches;
import static net.bytebuddy.matcher.ElementMatchers.namedOneOf;

/**
 * @author ebin
 */
public class LoggerInstrumentation implements TypeInstrumentation {
    @Override
    public ElementMatcher<TypeDescription> typeMatcher() {
        return AgentElementMatchers.hasSuperType(
            namedOneOf("org.slf4j.Logger"));
    }

    @Override
    public void transform(TypeTransformer typeTransformer) {
        typeTransformer.applyAdviceToMethod(
            nameMatches("(warn|error)")
                .and(
                    ElementMatchers.takesArgument(
                        0, ElementMatchers.named("org.slf4j.Marker")))
                .and(
                    ElementMatchers.takesArgument(
                        1, ElementMatchers.named("java.lang.String")))
                .and(ElementMatchers.isPublic()),
            this.getClass().getName() + "$ProcessLogWarnAdvice");

        typeTransformer.applyAdviceToMethod(
            nameMatches("(warn|error)")
                .and(
                    ElementMatchers.takesArgument(
                        0, ElementMatchers.named("java.lang.String")))
                .and(ElementMatchers.isPublic()),
            this.getClass().getName() + "$ProcessLogWarnAdvice");
    }

    @SuppressWarnings("unused")
    public static class ProcessLogWarnAdvice {

        @Advice.OnMethodEnter(suppress = Throwable.class)
        public static void onEnter(@Advice.Argument(value = 0) Marker marker) {
            Span current = Span.current();
            if (Objects.nonNull(marker)) {
                String errorCode = StringUtils.isEmpty(marker.getName()) ? "UNASSIGNED" : marker.getName();
                current.setStatus(StatusCode.ERROR, errorCode);
                current.setAttribute("error.code", errorCode);
            }
        }
    }
}
