package core.framework.javaagent.instrumentation;

import com.google.auto.service.AutoService;
import io.opentelemetry.javaagent.extension.instrumentation.InstrumentationModule;
import io.opentelemetry.javaagent.extension.instrumentation.TypeInstrumentation;

import java.util.Arrays;
import java.util.List;

/**
 * @author ebin
 */
@AutoService(InstrumentationModule.class)
public class CoreFrameworkInstrumentationModule extends InstrumentationModule {
    public static final String INSTRUMENTATION_NAME = "core-framework";

    public CoreFrameworkInstrumentationModule() {
        super(INSTRUMENTATION_NAME, "core-framework-1.0");
    }

    @Override
    public int order() {
        return 1;
    }


    @Override
    public List<TypeInstrumentation> typeInstrumentations() {
        return Arrays.asList(
            new DispatcherServletInstrumentation(),
            new LogbackInstrumentation(),
            new ExecutorInstrumentation()
        );
    }


    @Override
    public List<String> getAdditionalHelperClassNames() {
        return Arrays.asList(
            "core.framework.javaagent.instrumentation.ContextAwareRunnable",
            "core.framework.javaagent.instrumentation.ContextAwareCallable"
        );
    }

    @Override
    public boolean isHelperClass(String className) {
        return className.startsWith("core.framework.javaagent");
    }
}
