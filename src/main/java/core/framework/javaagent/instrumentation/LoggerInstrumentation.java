/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package core.framework.javaagent.instrumentation;

import io.opentelemetry.api.trace.Span;
import io.opentelemetry.javaagent.extension.instrumentation.TypeInstrumentation;
import io.opentelemetry.javaagent.extension.instrumentation.TypeTransformer;
import io.opentelemetry.javaagent.extension.matcher.AgentElementMatchers;
import net.bytebuddy.asm.Advice;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.matcher.ElementMatcher;
import net.bytebuddy.matcher.ElementMatchers;
import org.slf4j.Marker;
import org.slf4j.event.Level;
import org.slf4j.helpers.MessageFormatter;

import java.lang.reflect.Method;
import java.util.Optional;

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
                    ElementMatchers.takesArgument(0, ElementMatchers.named("org.slf4j.Marker"))
                )
                .and(
                    ElementMatchers.takesArgument(1, ElementMatchers.named("java.lang.String"))
                )
                .and(ElementMatchers.isPublic()),
            this.getClass().getName() + "$ProcessWithMarker");

        typeTransformer.applyAdviceToMethod(
            nameMatches("(warn|error)")
                .and(
                    ElementMatchers.takesArgument(0, ElementMatchers.named("java.lang.String"))
                )
                .and(ElementMatchers.isPublic()),
            this.getClass().getName() + "$ProcessWithOutMarker");
    }

    @SuppressWarnings("unused")
    public static class ProcessWithOutMarker {
        @Advice.OnMethodEnter(suppress = Throwable.class, inline = false)
        public static void onEnter(@Advice.Origin Method method,
                                   @Advice.AllArguments Object[] args) {
            Span span = Span.current();
            if (span != null) {
                Level level = "error".equals(method.getName()) ? Level.ERROR : Level.WARN;

                String errorMessage;
                if (args.length == 1) {
                    // void warn|error(String var1);
                    errorMessage = (String) args[0];
                } else if (args.length == 2) {
                    Object arg1 = args[1];
                    if (arg1 instanceof Throwable) {
                        // void warn(String var1, Throwable var2);
                        errorMessage = (String) args[0];
                    } else if (arg1 instanceof Object[]) {
                        // void warn(String var1, Object... var2);
                        errorMessage = MessageFormatter.arrayFormat((String) args[0], (Object[]) args[1]).getMessage();
                    } else {
                        // void warn(String var1, Object var2);
                        errorMessage = MessageFormatter.format((String) args[0], args[1]).getMessage();
                    }
                } else if (args.length == 3) {
                    // void warn(String var1, Object var2, Object var3);
                    errorMessage = MessageFormatter.format((String) args[0], args[1], args[2]).getMessage();
                } else {
                    errorMessage = "Uncaught method : " + method;
                }

                span.setAttribute("error.code", "UNASSIGNED");
                span.setAttribute("error.message", errorMessage);
                span.setAttribute("error.level", level.toString());
            }
        }
    }

    @SuppressWarnings("unused")
    public static class ProcessWithMarker {
        @Advice.OnMethodEnter(suppress = Throwable.class, inline = false)
        public static void onEnter(@Advice.Origin Method method,
                                   @Advice.AllArguments Object[] args) {
            Span span = Span.current();
            if (span != null) {
                Level level = "error".equals(method.getName()) ? Level.ERROR : Level.WARN;

                Marker marker = (Marker) args[0];
                String errorCode = Optional.ofNullable(marker)
                    .flatMap(m -> Optional.ofNullable(m.getName()))
                    .orElse("UNASSIGNED");

                String errorMessage;
                if (args.length == 2) {
                    // void warn(Marker var1, String var2);
                    errorMessage = (String) args[1];
                } else if (args.length == 3) {
                    Object arg2 = args[2];
                    if (arg2 instanceof Throwable) {
                        // void warn(Marker var1, String var2, Throwable var3);
                        errorMessage = (String) args[1];
                    } else if (arg2 instanceof Object[]) {
                        // void warn(Marker var1, String var2, Object... var3);
                        errorMessage = MessageFormatter.arrayFormat((String) args[1], (Object[]) args[2]).getMessage();
                    } else {
                        // void warn(Marker var1, String var2, Object var3);
                        errorMessage = MessageFormatter.format((String) args[1], args[2]).getMessage();
                    }
                } else if (args.length == 4) {
                    // void warn(Marker var1, String var2, Object var3, Object var4);
                    errorMessage = MessageFormatter.format((String) args[1], args[2], args[3]).getMessage();
                } else {
                    errorMessage = "Uncaught method : " + method;
                }

                span.setAttribute("error.code", errorCode);
                span.setAttribute("error.message", errorMessage);
                span.setAttribute("error.level", level.toString());
            }
        }
    }
}
