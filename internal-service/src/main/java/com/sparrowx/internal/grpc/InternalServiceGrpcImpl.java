package com.sparrowx.internal.grpc;

import buildingblocks.core.commands.CommandBus;
import buildingblocks.core.queries.QueryBus;
import com.sparrowx.internal.exceptions.InternalServiceException;
import com.sparrowx.internal.mappers.EngineerMapper;
import com.sparrowx.internal.mappers.InternalDocumentMapper;
import com.sparrowx.internal.mappers.InternalGraphMapper;
import com.sparrowx.internal.mappers.ModuleMapper;
import com.sparrowx.internal.mappers.OnboardingMapper;
import com.sparrowx.internal.mappers.PermissionMapper;
import com.sparrowx.internal.mappers.RepositoryMapper;
import com.sparrowx.internal.mappers.RunbookMapper;
import com.sparrowx.internal.mappers.TeamMapper;
import io.grpc.stub.StreamObserver;
import net.devh.boot.grpc.server.service.GrpcService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@GrpcService
public class InternalServiceGrpcImpl extends InternalServiceGrpc.InternalServiceImplBase {

    private static final Logger log =
            LoggerFactory.getLogger(InternalServiceGrpcImpl.class);

    private final CommandBus commandBus;
    private final QueryBus queryBus;

    public InternalServiceGrpcImpl(
            CommandBus commandBus,
            QueryBus queryBus
    ) {
        this.commandBus = commandBus;
        this.queryBus = queryBus;
    }

    @Override
    public void createEngineer(
            CreateEngineerRequest request,
            StreamObserver<CreateEngineerResponse> responseObserver
    ) {
        try {
            log.debug(
                    "CreateEngineer request tenantId={} email={}",
                    request.getContext().getTenantId(),
                    request.getEmail()
            );

            var command = EngineerMapper.toCreateEngineerCommand(request);
            var result = commandBus.dispatch(command);

            responseObserver.onNext(
                    EngineerMapper.toCreateEngineerResponse(result)
            );
            responseObserver.onCompleted();

        } catch (InternalServiceException ex) {
            log.error("CreateEngineer failed with InternalServiceException", ex);
            throw ex;

        } catch (Exception ex) {
            log.error("CreateEngineer failed", ex);
            throw new InternalServiceException("Failed to create engineer", ex);
        }
    }

    @Override
    public void getEngineer(
            GetEngineerRequest request,
            StreamObserver<GetEngineerResponse> responseObserver
    ) {
        try {
            log.debug(
                    "GetEngineer request tenantId={} engineerId={}",
                    request.getContext().getTenantId(),
                    request.getEngineerId()
            );

            var query = EngineerMapper.toGetEngineerQuery(request);
            var result = queryBus.dispatch(query);

            responseObserver.onNext(
                    EngineerMapper.toGetEngineerResponse(result)
            );
            responseObserver.onCompleted();

        } catch (InternalServiceException ex) {
            log.error("GetEngineer failed with InternalServiceException", ex);
            throw ex;

        } catch (Exception ex) {
            log.error("GetEngineer failed", ex);
            throw new InternalServiceException("Failed to get engineer", ex);
        }
    }

    @Override
    public void createTeam(
            CreateTeamRequest request,
            StreamObserver<CreateTeamResponse> responseObserver
    ) {
        try {
            var command = TeamMapper.toCreateTeamCommand(request);
            var result = commandBus.dispatch(command);

            responseObserver.onNext(
                    TeamMapper.toCreateTeamResponse(result)
            );
            responseObserver.onCompleted();

        } catch (InternalServiceException ex) {
            log.error("CreateTeam failed with InternalServiceException", ex);
            throw ex;

        } catch (Exception ex) {
            log.error("CreateTeam failed", ex);
            throw new InternalServiceException("Failed to create team", ex);
        }
    }

    @Override
    public void getTeam(
            GetTeamRequest request,
            StreamObserver<GetTeamResponse> responseObserver
    ) {
        try {
            var query = TeamMapper.toGetTeamQuery(request);
            var result = queryBus.dispatch(query);

            responseObserver.onNext(
                    TeamMapper.toGetTeamResponse(result)
            );
            responseObserver.onCompleted();

        } catch (InternalServiceException ex) {
            log.error("GetTeam failed with InternalServiceException", ex);
            throw ex;

        } catch (Exception ex) {
            log.error("GetTeam failed", ex);
            throw new InternalServiceException("Failed to get team", ex);
        }
    }

    @Override
    public void createModule(
            CreateModuleRequest request,
            StreamObserver<CreateModuleResponse> responseObserver
    ) {
        try {
            var command = ModuleMapper.toCreateModuleCommand(request);
            var result = commandBus.dispatch(command);

            responseObserver.onNext(
                    ModuleMapper.toCreateModuleResponse(result)
            );
            responseObserver.onCompleted();

        } catch (InternalServiceException ex) {
            log.error("CreateModule failed with InternalServiceException", ex);
            throw ex;

        } catch (Exception ex) {
            log.error("CreateModule failed", ex);
            throw new InternalServiceException("Failed to create module", ex);
        }
    }

    @Override
    public void getModule(
            GetModuleRequest request,
            StreamObserver<GetModuleResponse> responseObserver
    ) {
        try {
            var query = ModuleMapper.toGetModuleQuery(request);
            var result = queryBus.dispatch(query);

            responseObserver.onNext(
                    ModuleMapper.toGetModuleResponse(result)
            );
            responseObserver.onCompleted();

        } catch (InternalServiceException ex) {
            log.error("GetModule failed with InternalServiceException", ex);
            throw ex;

        } catch (Exception ex) {
            log.error("GetModule failed", ex);
            throw new InternalServiceException("Failed to get module", ex);
        }
    }

    @Override
    public void createRepository(
            CreateRepositoryRequest request,
            StreamObserver<CreateRepositoryResponse> responseObserver
    ) {
        try {
            var command = RepositoryMapper.toCreateRepositoryCommand(request);
            var result = commandBus.dispatch(command);

            responseObserver.onNext(
                    RepositoryMapper.toCreateRepositoryResponse(result)
            );
            responseObserver.onCompleted();

        } catch (InternalServiceException ex) {
            log.error("CreateRepository failed with InternalServiceException", ex);
            throw ex;

        } catch (Exception ex) {
            log.error("CreateRepository failed", ex);
            throw new InternalServiceException("Failed to create repository", ex);
        }
    }

    @Override
    public void getRepository(
            GetRepositoryRequest request,
            StreamObserver<GetRepositoryResponse> responseObserver
    ) {
        try {
            var query = RepositoryMapper.toGetRepositoryQuery(request);
            var result = queryBus.dispatch(query);

            responseObserver.onNext(
                    RepositoryMapper.toGetRepositoryResponse(result)
            );
            responseObserver.onCompleted();

        } catch (InternalServiceException ex) {
            log.error("GetRepository failed with InternalServiceException", ex);
            throw ex;

        } catch (Exception ex) {
            log.error("GetRepository failed", ex);
            throw new InternalServiceException("Failed to get repository", ex);
        }
    }

    @Override
    public void createInternalDocument(
            CreateInternalDocumentRequest request,
            StreamObserver<CreateInternalDocumentResponse> responseObserver
    ) {
        try {
            var command = InternalDocumentMapper.toCreateInternalDocumentCommand(request);
            var result = commandBus.dispatch(command);

            responseObserver.onNext(
                    InternalDocumentMapper.toCreateInternalDocumentResponse(result)
            );
            responseObserver.onCompleted();

        } catch (InternalServiceException ex) {
            log.error("CreateInternalDocument failed with InternalServiceException", ex);
            throw ex;

        } catch (Exception ex) {
            log.error("CreateInternalDocument failed", ex);
            throw new InternalServiceException("Failed to create internal document", ex);
        }
    }

    @Override
    public void getInternalDocument(
            GetInternalDocumentRequest request,
            StreamObserver<GetInternalDocumentResponse> responseObserver
    ) {
        try {
            var query = InternalDocumentMapper.toGetInternalDocumentQuery(request);
            var result = queryBus.dispatch(query);

            responseObserver.onNext(
                    InternalDocumentMapper.toGetInternalDocumentResponse(result)
            );
            responseObserver.onCompleted();

        } catch (InternalServiceException ex) {
            log.error("GetInternalDocument failed with InternalServiceException", ex);
            throw ex;

        } catch (Exception ex) {
            log.error("GetInternalDocument failed", ex);
            throw new InternalServiceException("Failed to get internal document", ex);
        }
    }

    @Override
    public void createRunbook(
            CreateRunbookRequest request,
            StreamObserver<CreateRunbookResponse> responseObserver
    ) {
        try {
            var command = RunbookMapper.toCreateRunbookCommand(request);
            var result = commandBus.dispatch(command);

            responseObserver.onNext(
                    RunbookMapper.toCreateRunbookResponse(result)
            );
            responseObserver.onCompleted();

        } catch (InternalServiceException ex) {
            log.error("CreateRunbook failed with InternalServiceException", ex);
            throw ex;

        } catch (Exception ex) {
            log.error("CreateRunbook failed", ex);
            throw new InternalServiceException("Failed to create runbook", ex);
        }
    }

    @Override
    public void getRunbook(
            GetRunbookRequest request,
            StreamObserver<GetRunbookResponse> responseObserver
    ) {
        try {
            var query = RunbookMapper.toGetRunbookQuery(request);
            var result = queryBus.dispatch(query);

            responseObserver.onNext(
                    RunbookMapper.toGetRunbookResponse(result)
            );
            responseObserver.onCompleted();

        } catch (InternalServiceException ex) {
            log.error("GetRunbook failed with InternalServiceException", ex);
            throw ex;

        } catch (Exception ex) {
            log.error("GetRunbook failed", ex);
            throw new InternalServiceException("Failed to get runbook", ex);
        }
    }

    @Override
    public void createOnboardingPath(
            CreateOnboardingPathRequest request,
            StreamObserver<CreateOnboardingPathResponse> responseObserver
    ) {
        try {
            var command = OnboardingMapper.toCreateOnboardingPathCommand(request);
            var result = commandBus.dispatch(command);

            responseObserver.onNext(
                    OnboardingMapper.toCreateOnboardingPathResponse(result)
            );
            responseObserver.onCompleted();

        } catch (InternalServiceException ex) {
            log.error("CreateOnboardingPath failed with InternalServiceException", ex);
            throw ex;

        } catch (Exception ex) {
            log.error("CreateOnboardingPath failed", ex);
            throw new InternalServiceException("Failed to create onboarding path", ex);
        }
    }

    @Override
    public void getOnboardingPath(
            GetOnboardingPathRequest request,
            StreamObserver<GetOnboardingPathResponse> responseObserver
    ) {
        try {
            var query = OnboardingMapper.toGetOnboardingPathQuery(request);
            var result = queryBus.dispatch(query);

            responseObserver.onNext(
                    OnboardingMapper.toGetOnboardingPathResponse(result)
            );
            responseObserver.onCompleted();

        } catch (InternalServiceException ex) {
            log.error("GetOnboardingPath failed with InternalServiceException", ex);
            throw ex;

        } catch (Exception ex) {
            log.error("GetOnboardingPath failed", ex);
            throw new InternalServiceException("Failed to get onboarding path", ex);
        }
    }

    @Override
    public void createOnboardingTask(
            CreateOnboardingTaskRequest request,
            StreamObserver<CreateOnboardingTaskResponse> responseObserver
    ) {
        try {
            var command = OnboardingMapper.toCreateOnboardingTaskCommand(request);
            var result = commandBus.dispatch(command);

            responseObserver.onNext(
                    OnboardingMapper.toCreateOnboardingTaskResponse(result)
            );
            responseObserver.onCompleted();

        } catch (InternalServiceException ex) {
            log.error("CreateOnboardingTask failed with InternalServiceException", ex);
            throw ex;

        } catch (Exception ex) {
            log.error("CreateOnboardingTask failed", ex);
            throw new InternalServiceException("Failed to create onboarding task", ex);
        }
    }

    @Override
    public void getOnboardingTask(
            GetOnboardingTaskRequest request,
            StreamObserver<GetOnboardingTaskResponse> responseObserver
    ) {
        try {
            var query = OnboardingMapper.toGetOnboardingTaskQuery(request);
            var result = queryBus.dispatch(query);

            responseObserver.onNext(
                    OnboardingMapper.toGetOnboardingTaskResponse(result)
            );
            responseObserver.onCompleted();

        } catch (InternalServiceException ex) {
            log.error("GetOnboardingTask failed with InternalServiceException", ex);
            throw ex;

        } catch (Exception ex) {
            log.error("GetOnboardingTask failed", ex);
            throw new InternalServiceException("Failed to get onboarding task", ex);
        }
    }

    @Override
    public void assignEngineerToOnboardingPath(
            AssignEngineerToOnboardingPathRequest request,
            StreamObserver<AssignEngineerToOnboardingPathResponse> responseObserver
    ) {
        try {
            var command = OnboardingMapper.toAssignEngineerToOnboardingPathCommand(request);
            var result = commandBus.dispatch(command);

            responseObserver.onNext(
                    OnboardingMapper.toAssignEngineerToOnboardingPathResponse(result)
            );
            responseObserver.onCompleted();

        } catch (InternalServiceException ex) {
            log.error("AssignEngineerToOnboardingPath failed with InternalServiceException", ex);
            throw ex;

        } catch (Exception ex) {
            log.error("AssignEngineerToOnboardingPath failed", ex);
            throw new InternalServiceException("Failed to assign engineer to onboarding path", ex);
        }
    }

    @Override
    public void completeOnboardingTask(
            CompleteOnboardingTaskRequest request,
            StreamObserver<CompleteOnboardingTaskResponse> responseObserver
    ) {
        try {
            var command = OnboardingMapper.toCompleteOnboardingTaskCommand(request);
            var result = commandBus.dispatch(command);

            responseObserver.onNext(
                    OnboardingMapper.toCompleteOnboardingTaskResponse(result)
            );
            responseObserver.onCompleted();

        } catch (InternalServiceException ex) {
            log.error("CompleteOnboardingTask failed with InternalServiceException", ex);
            throw ex;

        } catch (Exception ex) {
            log.error("CompleteOnboardingTask failed", ex);
            throw new InternalServiceException("Failed to complete onboarding task", ex);
        }
    }

    @Override
    public void getEngineerOnboardingProgress(
            GetEngineerOnboardingProgressRequest request,
            StreamObserver<GetEngineerOnboardingProgressResponse> responseObserver
    ) {
        try {
            var query = OnboardingMapper.toGetEngineerOnboardingProgressQuery(request);
            var result = queryBus.dispatch(query);

            responseObserver.onNext(
                    OnboardingMapper.toGetEngineerOnboardingProgressResponse(result)
            );
            responseObserver.onCompleted();

        } catch (InternalServiceException ex) {
            log.error("GetEngineerOnboardingProgress failed with InternalServiceException", ex);
            throw ex;

        } catch (Exception ex) {
            log.error("GetEngineerOnboardingProgress failed", ex);
            throw new InternalServiceException("Failed to get engineer onboarding progress", ex);
        }
    }

    @Override
    public void createPermission(
            CreatePermissionRequest request,
            StreamObserver<CreatePermissionResponse> responseObserver
    ) {
        try {
            var command = PermissionMapper.toCreatePermissionCommand(request);
            var result = commandBus.dispatch(command);

            responseObserver.onNext(
                    PermissionMapper.toCreatePermissionResponse(result)
            );
            responseObserver.onCompleted();

        } catch (InternalServiceException ex) {
            log.error("CreatePermission failed with InternalServiceException", ex);
            throw ex;

        } catch (Exception ex) {
            log.error("CreatePermission failed", ex);
            throw new InternalServiceException("Failed to create permission", ex);
        }
    }

    @Override
    public void getPermission(
            GetPermissionRequest request,
            StreamObserver<GetPermissionResponse> responseObserver
    ) {
        try {
            var query = PermissionMapper.toGetPermissionQuery(request);
            var result = queryBus.dispatch(query);

            responseObserver.onNext(
                    PermissionMapper.toGetPermissionResponse(result)
            );
            responseObserver.onCompleted();

        } catch (InternalServiceException ex) {
            log.error("GetPermission failed with InternalServiceException", ex);
            throw ex;

        } catch (Exception ex) {
            log.error("GetPermission failed", ex);
            throw new InternalServiceException("Failed to get permission", ex);
        }
    }

    @Override
    public void readInternalCompanyGraph(
            ReadInternalCompanyGraphRequest request,
            StreamObserver<ReadInternalCompanyGraphResponse> responseObserver
    ) {
        try {
            var query = InternalGraphMapper.toReadInternalCompanyGraphQuery(request);
            var result = queryBus.dispatch(query);

            responseObserver.onNext(
                    InternalGraphMapper.toReadInternalCompanyGraphResponse(result)
            );
            responseObserver.onCompleted();

        } catch (InternalServiceException ex) {
            log.error("ReadInternalCompanyGraph failed with InternalServiceException", ex);
            throw ex;

        } catch (Exception ex) {
            log.error("ReadInternalCompanyGraph failed", ex);
            throw new InternalServiceException("Failed to read internal company graph", ex);
        }
    }

    @Override
    public void readLearningGraph(
            ReadLearningGraphRequest request,
            StreamObserver<ReadLearningGraphResponse> responseObserver
    ) {
        try {
            var query = InternalGraphMapper.toReadLearningGraphQuery(request);
            var result = queryBus.dispatch(query);

            responseObserver.onNext(
                    InternalGraphMapper.toReadLearningGraphResponse(result)
            );
            responseObserver.onCompleted();

        } catch (InternalServiceException ex) {
            log.error("ReadLearningGraph failed with InternalServiceException", ex);
            throw ex;

        } catch (Exception ex) {
            log.error("ReadLearningGraph failed", ex);
            throw new InternalServiceException("Failed to read learning graph", ex);
        }
    }
}