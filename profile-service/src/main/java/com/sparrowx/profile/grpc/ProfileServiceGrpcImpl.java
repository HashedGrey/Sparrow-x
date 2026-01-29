package com.sparrowx.profile.grpc;

import com.example.profile.grpc.CreateProfileRequest;
import com.sparrowx.profile.dtos.ProfileDto;
import com.sparrowx.profile.features.profile.queries.GetProfileByIdQuery;
import com.sparrowx.profile.grpc.ProfileRequestDto;
import com.sparrowx.profile.grpc.ProfileResponseDto;
import com.sparrowx.profile.grpc.ProfileServiceGrpc;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import io.grpc.stub.StreamObserver;
import net.devh.boot.grpc.server.service.GrpcService;

import java.util.UUID;

import static com.sparrowx.profile.features.Mappings.toProfileResponseDtoGrpc;

@GrpcService
public class ProfileGrpcService extends ProfileServiceGrpc.ProfileServiceImplBase {

    private final LocalCommandBus commandBus;

    public ProfileGrpcService(LocalCommandBus commandBus) {
        this.commandBus = commandBus;
    }

    @Override
    public void createProfile(CreateProfileRequest request,
                              StreamObserver<ProfileResponse> responseObserver) {

        try {
            var command = Mappings.toCreateProfileCommand(request);
            ProfileDto result = commandBus.send(command);

            responseObserver.onNext(Mappings.toProfileResponseDtoGrpc(result));
            responseObserver.onCompleted();

        } catch (Exception ex) {
            responseObserver.onError(toStatus(ex));
        }
    }

    @Override
    public void getProfileById(GetProfileByIdRequest request,
                               StreamObserver<ProfileResponse> responseObserver) {

        try {
            var query = new GetProfileByIdQuery(UUID.fromString(request.getId()));
            ProfileDto result = commandBus.send(query);

            responseObserver.onNext(Mappings.toProfileResponseDtoGrpc(result));
            responseObserver.onCompleted();

        } catch (Exception ex) {
            responseObserver.onError(toStatus(ex));
        }
    }

    @Override
    public void updateProfile(UpdateProfileRequest request,
                              StreamObserver<ProfileResponse> responseObserver) {

        try {
            var command = Mappings.toUpdateProfileCommand(request);
            ProfileDto result = commandBus.send(command);

            responseObserver.onNext(Mappings.toProfileResponseDtoGrpc(result));
            responseObserver.onCompleted();

        } catch (Exception ex) {
            responseObserver.onError(toStatus(ex));
        }
    }

    @Override
    public void deleteProfile(DeleteProfileRequest request,
                              StreamObserver<DeleteProfileResponse> responseObserver) {

        try {
            var command = new DeleteProfileCommand(UUID.fromString(request.getId()));
            commandBus.send(command);

            responseObserver.onNext(
                    DeleteProfileResponse.newBuilder().setDeleted(true).build()
            );
            responseObserver.onCompleted();

        } catch (Exception ex) {
            responseObserver.onError(toStatus(ex));
        }
    }

    private StatusRuntimeException toStatus(Exception ex) {
        if (ex instanceof IllegalArgumentException) {
            return Status.INVALID_ARGUMENT.withDescription(ex.getMessage()).asRuntimeException();
        }
        return Status.INTERNAL.withDescription(ex.getMessage()).asRuntimeException();
    }
}

