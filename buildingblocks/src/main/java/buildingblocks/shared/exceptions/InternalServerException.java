package buildingblocks.shared.exceptions;

import org.springframework.http.HttpStatus;
import java.util.List;

public class InternalServerException extends CustomException {

    public InternalServerException(String message) {
        super(message, HttpStatus.INTERNAL_SERVER_ERROR, List.of(message));
    }

    public InternalServerException(String message, Integer code) {
        super(message, HttpStatus.INTERNAL_SERVER_ERROR, List.of(message));
    }

    public InternalServerException(String message, Integer code, Object... args) {
        super(String.format(message, args), HttpStatus.INTERNAL_SERVER_ERROR, List.of(String.format(message, args)));
    }

    public InternalServerException(String message, Exception innerException) {
        super(message, HttpStatus.INTERNAL_SERVER_ERROR, List.of(message));
        initCause(innerException);
    }
}

