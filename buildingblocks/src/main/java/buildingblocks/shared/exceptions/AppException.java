package buildingblocks.shared.exceptions;

import org.springframework.http.HttpStatus;
import java.util.List;

public class AppException extends CustomException {

    public AppException(String message, Integer code) {
        super(message, HttpStatus.BAD_REQUEST, List.of(message));
    }

    public AppException(String message, HttpStatus statusCode, Integer code) {
        super(message, statusCode, List.of(message));
    }

    public AppException(String message, Exception innerException, Integer code) {
        super(message, HttpStatus.BAD_REQUEST, List.of(message));
        initCause(innerException);
    }

    public AppException() {
        super("Application exception occurred", HttpStatus.BAD_REQUEST, List.of("Application exception occurred"));
    }
}
