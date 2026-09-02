package com.sparrowx.document.grpc;

import buildingblocks.core.commands.CommandBus;
import buildingblocks.core.queries.QueryBus;
import com.sparrowx.document.features.builddocumentevidence.BuildDocumentEvidenceCommand;
import com.sparrowx.document.features.builddocumentevidence.BuildDocumentEvidenceResult;
import com.sparrowx.document.features.getdocument.GetDocumentQuery;
import com.sparrowx.document.features.getdocument.GetDocumentResult;
import com.sparrowx.document.features.getingestionjob.GetIngestionJobQuery;
import com.sparrowx.document.features.getingestionjob.GetIngestionJobResult;
import com.sparrowx.document.features.searchdocumentspans.SearchDocumentSpansQuery;
import com.sparrowx.document.features.searchdocumentspans.SearchDocumentSpansResult;
import com.sparrowx.document.features.uploaddocument.UploadDocumentCommand;
import com.sparrowx.document.features.uploaddocument.UploadDocumentResult;
import com.sparrowx.document.features.verifyevidencegraph.VerifyEvidenceGraphQuery;
import com.sparrowx.document.features.verifyevidencegraph.VerifyEvidenceGraphResult;
import com.sparrowx.document.mappers.DocumentMapper;
import com.sparrowx.document.proto.BuildDocumentEvidenceRequest;
import com.sparrowx.document.proto.BuildDocumentEvidenceResponse;
import com.sparrowx.document.proto.DocumentServiceGrpc;
import com.sparrowx.document.proto.GetDocumentRequest;
import com.sparrowx.document.proto.GetDocumentResponse;
import com.sparrowx.document.proto.GetIngestionJobRequest;
import com.sparrowx.document.proto.GetIngestionJobResponse;
import com.sparrowx.document.proto.RequestContext;
import com.sparrowx.document.proto.SearchDocumentSpansRequest;
import com.sparrowx.document.proto.SearchDocumentSpansResponse;
import com.sparrowx.document.proto.UploadDocumentRequest;
import com.sparrowx.document.proto.UploadDocumentResponse;
import com.sparrowx.document.proto.VerifyEvidenceGraphRequest;
import com.sparrowx.document.proto.VerifyEvidenceGraphResponse;
import io.grpc.stub.StreamObserver;
import org.slf4j.MDC;
import org.springframework.grpc.server.service.GrpcService;

@GrpcService
public class DocumentServiceGrpcImpl extends DocumentServiceGrpc.DocumentServiceImplBase {

    private static final String MDC_REQUEST_ID = "request_id";
    private static final String MDC_BUSINESS_TRACE_ID = "business_trace_id";
    private static final String MDC_TENANT_ID = "tenant_id";
    private static final String MDC_USER_ID = "user_id";
    private static final String MDC_PROJECT_ID = "project_id";
    private static final String MDC_TEAM_ID = "team_id";
    private static final String MDC_CALLER_SERVICE = "caller_service";

    private final CommandBus commandBus;
    private final QueryBus queryBus;
    private final DocumentMapper mapper;

    public DocumentServiceGrpcImpl(
            CommandBus commandBus,
            QueryBus queryBus,
            DocumentMapper mapper
    ) {
        this.commandBus = commandBus;
        this.queryBus = queryBus;
        this.mapper = mapper;
    }

    @Override
    public void uploadDocument(
            UploadDocumentRequest request,
            StreamObserver<UploadDocumentResponse> responseObserver
    ) {
        try {
            putContextInMdc(request.getContext());

            UploadDocumentCommand command = mapper.toUploadDocumentCommand(request);
            UploadDocumentResult result = commandBus.dispatch(command);

            responseObserver.onNext(mapper.toUploadDocumentResponse(result));
            responseObserver.onCompleted();
        } finally {
            clearBusinessMdc();
        }
    }

    @Override
    public void getDocument(
            GetDocumentRequest request,
            StreamObserver<GetDocumentResponse> responseObserver
    ) {
        try {
            putContextInMdc(request.getContext());

            GetDocumentQuery query = mapper.toGetDocumentQuery(request);
            GetDocumentResult result = queryBus.dispatch(query);

            responseObserver.onNext(mapper.toGetDocumentResponse(result));
            responseObserver.onCompleted();
        } finally {
            clearBusinessMdc();
        }
    }

    @Override
    public void getIngestionJob(
            GetIngestionJobRequest request,
            StreamObserver<GetIngestionJobResponse> responseObserver
    ) {
        try {
            putContextInMdc(request.getContext());

            GetIngestionJobQuery query = mapper.toGetIngestionJobQuery(request);
            GetIngestionJobResult result = queryBus.dispatch(query);

            responseObserver.onNext(mapper.toGetIngestionJobResponse(result));
            responseObserver.onCompleted();
        } finally {
            clearBusinessMdc();
        }
    }

    @Override
    public void searchDocumentSpans(
            SearchDocumentSpansRequest request,
            StreamObserver<SearchDocumentSpansResponse> responseObserver
    ) {
        try {
            putContextInMdc(request.getContext());

            SearchDocumentSpansQuery query = mapper.toSearchDocumentSpansQuery(request);
            SearchDocumentSpansResult result = queryBus.dispatch(query);

            responseObserver.onNext(mapper.toSearchDocumentSpansResponse(result));
            responseObserver.onCompleted();
        } finally {
            clearBusinessMdc();
        }
    }

    @Override
    public void buildDocumentEvidence(
            BuildDocumentEvidenceRequest request,
            StreamObserver<BuildDocumentEvidenceResponse> responseObserver
    ) {
        try {
            putContextInMdc(request.getContext());

            BuildDocumentEvidenceCommand command = mapper.toBuildDocumentEvidenceCommand(request);
            BuildDocumentEvidenceResult result = commandBus.dispatch(command);

            responseObserver.onNext(mapper.toBuildDocumentEvidenceResponse(result));
            responseObserver.onCompleted();
        } finally {
            clearBusinessMdc();
        }
    }

    @Override
    public void verifyEvidenceGraph(
            VerifyEvidenceGraphRequest request,
            StreamObserver<VerifyEvidenceGraphResponse> responseObserver
    ) {
        try {
            putContextInMdc(request.getContext());

            VerifyEvidenceGraphQuery query = mapper.toVerifyEvidenceGraphQuery(request);
            VerifyEvidenceGraphResult result = queryBus.dispatch(query);

            responseObserver.onNext(mapper.toVerifyEvidenceGraphResponse(result));
            responseObserver.onCompleted();
        } finally {
            clearBusinessMdc();
        }
    }

    private void putContextInMdc(RequestContext context) {
        if (context == null) {
            return;
        }

        putIfPresent(MDC_REQUEST_ID, context.getRequestId());
        putIfPresent(MDC_BUSINESS_TRACE_ID, context.getTraceId());
        putIfPresent(MDC_TENANT_ID, context.getTenantId());
        putIfPresent(MDC_USER_ID, context.getUserId());
        putIfPresent(MDC_PROJECT_ID, context.getProjectId());
        putIfPresent(MDC_TEAM_ID, context.getTeamId());
        putIfPresent(MDC_CALLER_SERVICE, context.getCallerService());
    }

    private void putIfPresent(String key, String value) {
        if (value != null && !value.isBlank()) {
            MDC.put(key, value);
        }
    }

    private void clearBusinessMdc() {
        MDC.remove(MDC_REQUEST_ID);
        MDC.remove(MDC_BUSINESS_TRACE_ID);
        MDC.remove(MDC_TENANT_ID);
        MDC.remove(MDC_USER_ID);
        MDC.remove(MDC_PROJECT_ID);
        MDC.remove(MDC_TEAM_ID);
        MDC.remove(MDC_CALLER_SERVICE);
    }
}