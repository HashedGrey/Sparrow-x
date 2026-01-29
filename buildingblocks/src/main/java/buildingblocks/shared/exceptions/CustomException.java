package buildingblocks.shared.exceptions;
import lombok.Getter;
import org.springframework.http.HttpStatus;
import java.util.List;

@Getter
public abstract class CustomException extends RuntimeException {
    private final HttpStatus status;
    private final List<String> errorMessages;

    protected CustomException(String message, HttpStatus status, List<String> errorMessages) {
        super(message);
        this.status = status;
        this.errorMessages = errorMessages;
    }

    protected CustomException(String message, HttpStatus status) {
        this(message, status, List.of(message));
    }

}