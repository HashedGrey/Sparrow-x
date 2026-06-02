package com.sparrowx.document.ingestion;

import buildingblocks.core.commands.CommandBus;
import com.sparrowx.document.features.processingestionjob.ProcessIngestionJobCommand;
import com.sparrowx.document.features.processingestionjob.ProcessIngestionJobResult;
import com.sparrowx.document.ingestion.queue.IngestionQueueMessage;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.SpanContext;
import io.opentelemetry.api.trace.TraceFlags;
import io.opentelemetry.api.trace.TraceState;
import io.opentelemetry.context.Context;
import io.opentelemetry.context.Scope;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class IngestionJobRunner {

    private final CommandBus commandBus;

    public IngestionJobRunner(CommandBus commandBus) {
        this.commandBus = commandBus;
    }

    public ProcessIngestionJobResult run(IngestionQueueMessage message) {
        if (message == null) {
            throw new IllegalArgumentException("IngestionQueueMessage must not be null");
        }

        Context parentContext = restoreParentContext(message);
        Map<String, String> previousMdc = MDC.getCopyOfContextMap();

        try (Scope ignored = parentContext.makeCurrent()) {
            populateWorkerMdc(message);

            return commandBus.dispatch(new ProcessIngestionJobCommand(
                    message.ingestionJobId(),
                    message.documentId(),
                    message.tenantId(),
                    message.projectId(),
                    message.teamId(),
                    message.objectKey(),
                    message.fileName(),
                    message.mimeType()
            ));
        } finally {
            restoreMdc(previousMdc);
        }
    }

    private Context restoreParentContext(IngestionQueueMessage message) {
        if (message.parentTraceId() == null || message.parentSpanId() == null) {
            return Context.current();
        }

        SpanContext parentSpanContext = SpanContext.createFromRemoteParent(
                message.parentTraceId(),
                message.parentSpanId(),
                TraceFlags.fromHex(message.traceFlags() == null ? "01" : message.traceFlags(), 0),
                TraceState.getDefault()
        );

        if (!parentSpanContext.isValid()) {
            return Context.current();
        }

        return Context.current().with(Span.wrap(parentSpanContext));
    }

    private void populateWorkerMdc(IngestionQueueMessage message) {
        SpanContext spanContext = Span.current().getSpanContext();

        if (spanContext.isValid()) {
            MDC.put("otel_trace_id", spanContext.getTraceId());
            MDC.put("otel_span_id", spanContext.getSpanId());
        }

        putIfPresent("request_id", message.requestId());
        putIfPresent("business_trace_id", message.businessTraceId());
        putIfPresent("tenant_id", message.tenantId());
        putIfPresent("document_id", message.documentId());
        putIfPresent("ingestion_job_id", message.ingestionJobId());
    }

    private void putIfPresent(String key, Object value) {
        if (value != null) {
            MDC.put(key, value.toString());
        }
    }

    private void restoreMdc(Map<String, String> previousMdc) {
        MDC.clear();

        if (previousMdc != null) {
            MDC.setContextMap(previousMdc);
        }
    }
}