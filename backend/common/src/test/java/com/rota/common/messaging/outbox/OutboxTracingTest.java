package com.rota.common.messaging.outbox;

import io.micrometer.tracing.Span;
import io.micrometer.tracing.Tracer;
import io.micrometer.tracing.otel.bridge.OtelCurrentTraceContext;
import io.micrometer.tracing.otel.bridge.OtelPropagator;
import io.micrometer.tracing.otel.bridge.OtelTracer;
import io.opentelemetry.api.trace.propagation.W3CTraceContextPropagator;
import io.opentelemetry.context.propagation.ContextPropagators;
import io.opentelemetry.sdk.trace.SdkTracerProvider;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;

class OutboxTracingTest {

    private final io.opentelemetry.api.trace.Tracer otel = SdkTracerProvider.builder().build().get("test");
    private final Tracer tracer = new OtelTracer(otel, new OtelCurrentTraceContext(), event -> { });
    private final OutboxTracing tracing = new OutboxTracing(tracer,
            new OtelPropagator(ContextPropagators.create(W3CTraceContextPropagator.getInstance()), otel));

    @Test
    void envioDoRelayContinuaOTraceDaRequisicao() throws Exception {
        Span request = tracer.nextSpan().name("POST /orders/checkout").start();
        String traceParent;
        try (Tracer.SpanInScope ignored = tracer.withSpan(request)) {
            traceParent = tracing.currentTraceParent();
        } finally {
            request.end();
        }
        assertThat(traceParent).startsWith("00-" + request.context().traceId() + "-");

        AtomicReference<Span> duringSend = new AtomicReference<>();
        tracing.inTrace(traceParent, "outbox order.status-changed", () -> duringSend.set(tracer.currentSpan()));

        assertThat(duringSend.get().context().traceId()).isEqualTo(request.context().traceId());
        assertThat(duringSend.get().context().parentId()).isEqualTo(request.context().spanId());
        assertThat(tracer.currentSpan()).isNull();
    }

    @Test
    void foraDeUmTraceNaoGravaContexto() throws Exception {
        assertThat(tracing.currentTraceParent()).isNull();

        boolean[] ran = {false};
        tracing.inTrace(null, "outbox x", () -> ran[0] = true);
        assertThat(ran[0]).isTrue();
    }
}
