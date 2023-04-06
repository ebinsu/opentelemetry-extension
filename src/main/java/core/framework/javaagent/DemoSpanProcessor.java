/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package core.framework.javaagent;

import io.opentelemetry.api.common.AttributeType;
import io.opentelemetry.api.internal.InternalAttributeKeyImpl;
import io.opentelemetry.api.trace.StatusCode;
import io.opentelemetry.context.Context;
import io.opentelemetry.sdk.autoconfigure.spi.AutoConfigurationCustomizerProvider;
import io.opentelemetry.sdk.common.CompletableResultCode;
import io.opentelemetry.sdk.trace.ReadWriteSpan;
import io.opentelemetry.sdk.trace.ReadableSpan;
import io.opentelemetry.sdk.trace.SpanProcessor;
import io.opentelemetry.testing.internal.io.netty.util.AttributeKey;
import org.apache.commons.lang3.RandomStringUtils;

import java.util.logging.Logger;

/**
 * See <a
 * href="https://github.com/open-telemetry/opentelemetry-specification/blob/master/specification/trace/sdk.md#span-processor">
 * OpenTelemetry Specification</a> for more information about {@link SpanProcessor}.
 *
 * @see AutoConfigurationCustomizerProvider
 */
public class DemoSpanProcessor implements SpanProcessor {
    private final Logger logger = Logger.getLogger(DemoSpanProcessor.class.getName());

    @Override
    public void onStart(Context parentContext, ReadWriteSpan span) {
    /*
    The sole purpose of this attribute is to introduce runtime dependency on some external library.
    We need this to demonstrate how extension can use them.
     */
        span.setStatus(StatusCode.OK);
        logger.warning("start name: " + span.getName());
    }

    @Override
    public boolean isStartRequired() {
        return true;
    }

    @Override
    public void onEnd(ReadableSpan span) {

        logger.warning("end name: " + span.getName() + " " + span.getKind());
    }

    @Override
    public boolean isEndRequired() {
        return true;
    }

    @Override
    public CompletableResultCode shutdown() {
        return CompletableResultCode.ofSuccess();
    }

    @Override
    public CompletableResultCode forceFlush() {
        return CompletableResultCode.ofSuccess();
    }
}
