package core.framework.javaagent.instrumentation;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.spi.ILoggingEvent;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.StatusCode;
import io.opentelemetry.javaagent.extension.instrumentation.TypeInstrumentation;
import io.opentelemetry.javaagent.extension.instrumentation.TypeTransformer;
import net.bytebuddy.asm.Advice;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.matcher.ElementMatcher;
import org.slf4j.MDC;
import org.slf4j.Marker;

import java.util.List;

import static net.bytebuddy.matcher.ElementMatchers.isMethod;
import static net.bytebuddy.matcher.ElementMatchers.isPublic;
import static net.bytebuddy.matcher.ElementMatchers.named;
import static net.bytebuddy.matcher.ElementMatchers.takesArgument;
import static net.bytebuddy.matcher.ElementMatchers.takesArguments;

/**
 * @author ebin
 */
public class LogbackSpanAttributeInstrumentation implements TypeInstrumentation {
    @Override
    public ElementMatcher<TypeDescription> typeMatcher() {
        return named("ch.qos.logback.classic.Logger");
    }

    @Override
    public void transform(TypeTransformer transformer) {
        transformer.applyAdviceToMethod(
            isMethod()
                .and(isPublic())
                .and(named("callAppenders"))
                .and(takesArguments(1))
                .and(takesArgument(0, named("ch.qos.logback.classic.spi.ILoggingEvent"))),
            LogbackSpanAttributeInstrumentation.class.getName() + "$CallAppendersAdvice");
    }

    @SuppressWarnings("unused")
    public static class CallAppendersAdvice {

        @Advice.OnMethodEnter(suppress = Throwable.class)
        public static void methodEnter(@Advice.Argument(0) ILoggingEvent event) {
            Level level = event.getLevel();
            if (Level.INFO == level) {
                List<Marker> markerList = event.getMarkerList();
                if (markerList != null && !markerList.isEmpty()) {
                    boolean record = "log-attribute".equals(markerList.get(0).getName());
                    if (record) {
                        Span span = Span.current();
                        if (span != null) {
                            String formattedMessage = event.getFormattedMessage();
                            String[] split = formattedMessage.split("=");
                            if (split.length == 2) {
                                span.setAttribute(split[0].trim(), split[1].trim());
                            }
                        }
                    }
                }
            } else if (Level.WARN == level || Level.ERROR == level) {
                boolean updateError;
                String preLevelStr = MDC.get("otel.span.scope.error.level");
                if (preLevelStr == null) {
                    MDC.put("otel.span.scope.error.level", level.toString());
                    updateError = true;
                } else {
                    Level preLevel = Level.valueOf(preLevelStr);
                    updateError = level.toInt() > preLevel.toInt();
                }

                if (!updateError) {
                    return;
                }

                Span span = Span.current();
                if (span != null) {
                    List<Marker> markerList = event.getMarkerList();
                    String errorCode;
                    if (markerList == null || markerList.isEmpty()) {
                        errorCode = "UNASSIGNED";
                    } else {
                        String name = markerList.get(0).getName();
                        if (name == null) {
                            errorCode = "UNASSIGNED";
                        } else {
                            errorCode = name;
                        }
                    }
                    span.setStatus(StatusCode.ERROR, errorCode);
                    span.setAttribute("error.code", errorCode);
                    span.setAttribute("error.message", event.getFormattedMessage());
                    span.setAttribute("error.level", level.toString());
                }
            }
        }
    }
}
