package buildingblocks.shared.exceptions;

import org.springframework.http.HttpStatus;
import java.util.List;

public class IdentityException extends CustomException {

    public IdentityException(String message, HttpStatus status) {
        super(message, status);
    }

    public IdentityException(String message, HttpStatus status, List<String> errors) {
        super(message, status, errors);
    }

    public IdentityException(String message) {
        super(message, HttpStatus.BAD_REQUEST);
    }
}
