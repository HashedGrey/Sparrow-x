package buildingblocks.shared.exceptions;

import io.grpc.Status;
import java.util.List;

public class StatusWithBody {

    private final Status status;
    private final List<String> errorMessages;

    public StatusWithBody(Status status, List<String> errorMessages) {
        this.status = status;
        this.errorMessages = errorMessages;
    }

    public Status getStatus() {
        return status;
    }

    public List<String> getErrorMessages() {
        return errorMessages;
    }
}
