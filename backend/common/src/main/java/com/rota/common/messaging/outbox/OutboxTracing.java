package com.rota.common.messaging.outbox;

import io.micrometer.tracing.Span;
import io.micrometer.tracing.Tracer;
import io.micrometer.tracing.propagation.Propagator;

import java.util.HashMap;
import java.util.Map;

/**
 * Liga o trace da requisição que gravou o evento ao envio feito depois pelo relay.
 * O {@code traceparent} (W3C) é guardado na linha do outbox; na publicação, o relay abre um
 * span filho dele, e a instrumentação do RabbitMQ leva o contexto até os consumidores.
 * Sem tracing configurado, nada é gravado e o envio segue sem span.
 */
public class OutboxTracing {

    static final String TRACEPARENT = "traceparent";

    static final OutboxTracing NOOP = new OutboxTracing(null, null);

    private final Tracer tracer;
    private final Propagator propagator;

    public OutboxTracing(Tracer tracer, Propagator propagator) {
        this.tracer = tracer;
        this.propagator = propagator;
    }

    /** {@code traceparent} do span atual, ou {@code null} fora de um trace. */
    String currentTraceParent() {
        if (tracer == null) {
            return null;
        }
        Span span = tracer.currentSpan();
        if (span == null) {
            return null;
        }
        Map<String, String> carrier = new HashMap<>();
        propagator.inject(span.context(), carrier, Map::put);
        return carrier.get(TRACEPARENT);
    }

    /** Executa o envio dentro de um span filho do trace de origem. */
    void inTrace(String traceParent, String spanName, Action action) throws Exception {
        if (tracer == null || traceParent == null) {
            action.run();
            return;
        }
        Span span = propagator.extract(Map.of(TRACEPARENT, traceParent), Map::get)
                .name(spanName)
                .start();
        try (Tracer.SpanInScope ignored = tracer.withSpan(span)) {
            action.run();
        } catch (Exception e) {
            span.error(e);
            throw e;
        } finally {
            span.end();
        }
    }

    @FunctionalInterface
    interface Action {
        void run() throws Exception;
    }
}
