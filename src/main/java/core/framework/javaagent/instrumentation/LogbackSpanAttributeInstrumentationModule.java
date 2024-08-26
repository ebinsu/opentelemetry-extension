/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package core.framework.javaagent.instrumentation;

import com.google.auto.service.AutoService;
import io.opentelemetry.javaagent.extension.instrumentation.InstrumentationModule;
import io.opentelemetry.javaagent.extension.instrumentation.TypeInstrumentation;
import io.opentelemetry.javaagent.extension.matcher.AgentElementMatchers;
import net.bytebuddy.matcher.ElementMatcher;

import java.util.List;

import static java.util.Collections.singletonList;

/**
 * @author ebin
 */
@AutoService(InstrumentationModule.class)
public class LogbackSpanAttributeInstrumentationModule extends InstrumentationModule {
    public LogbackSpanAttributeInstrumentationModule() {
        super("logback-span-attr", "logback-span-attr-1.0");
    }

    @Override
    public ElementMatcher.Junction<ClassLoader> classLoaderMatcher() {
        return AgentElementMatchers.hasClassesNamed("ch.qos.logback.classic.Logger", "org.slf4j.MDC");
    }

    @Override
    public List<TypeInstrumentation> typeInstrumentations() {
        return singletonList(new LogbackSpanAttributeInstrumentation());
    }
}
