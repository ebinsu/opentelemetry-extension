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
public final class ServletInitialHandlerInstrumentationModule extends InstrumentationModule {
    public ServletInitialHandlerInstrumentationModule() {
        super("servlet-initial-handler", "handle-request");
    }

    @Override
    public int order() {
        return 1;
    }

    @Override
    public ElementMatcher.Junction<ClassLoader> classLoaderMatcher() {
        return AgentElementMatchers.hasClassesNamed(
                "io.undertow.servlet.handlers.ServletInitialHandler");
    }

    @Override
    public List<TypeInstrumentation> typeInstrumentations() {
        return singletonList(new ServletInitialHandlerInstrumentation());
    }
}
