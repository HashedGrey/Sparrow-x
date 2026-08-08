package com.sparrowx.agentic.steps;

import com.sparrowx.agentic.actions.document.BuildDocumentEvidenceAction;
import com.sparrowx.agentic.tools.document.DocumentEvidenceRequestBuilder.BuildSpec;
import com.sparrowx.agentic.validation.DownstreamResponseValidator;
import com.sparrowx.agentic.validation.DownstreamResponseValidator.ResponseMetadata;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component
public final class BuildDocumentEvidenceStep {

    private final BuildDocumentEvidenceAction action;
    private final DownstreamResponseValidator responseValidator;

    public BuildDocumentEvidenceStep(
            BuildDocumentEvidenceAction action,
            DownstreamResponseValidator responseValidator
    ) {
        this.action = Objects.requireNonNull(
                action,
                "action must not be null"
        );
        this.responseValidator = Objects.requireNonNull(
                responseValidator,
                "responseValidator must not be null"
        );
    }

    public BuildDocumentEvidenceAction.Result execute(
            MissionContext context,
            BuildSpec spec
    ) {
        Objects.requireNonNull(context, "context must not be null");
        Objects.requireNonNull(spec, "spec must not be null");

        BuildDocumentEvidenceAction.Result result =
                action.execute(context, spec);

        return responseValidator.validateDocument(
                "build-document-evidence",
                context.tenantId(),
                result,
                value -> new ResponseMetadata(
                        context.tenantId(),
                        spec.requestId(),
                        spec.requestId() + ":document-evidence",
                        value.evidenceRefs().size(),
                        value.graph().getSerializedSize(),
                        value.evidenceRefs()
                )
        );
    }
}