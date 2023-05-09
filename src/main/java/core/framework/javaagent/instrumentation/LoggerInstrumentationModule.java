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
public class LoggerInstrumentationModule extends InstrumentationModule {
    public LoggerInstrumentationModule() {
        super("logger", "logger-warn");
    }

    @Override
    public int order() {
        return 2;
    }

    @Override
    public ElementMatcher.Junction<ClassLoader> classLoaderMatcher() {
        return AgentElementMatchers.hasClassesNamed("org.slf4j.Logger");
    }

    @Override
    public List<TypeInstrumentation> typeInstrumentations() {
        return singletonList(new LoggerInstrumentation());
    }
}
