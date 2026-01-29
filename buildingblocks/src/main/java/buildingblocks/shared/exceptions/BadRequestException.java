package buildingblocks.shared.exceptions;

import org.springframework.http.HttpStatus;
import java.util.List;

public class BadRequestException extends CustomException {
    public BadRequestException(String message, List<String> errors) {
        super(message, HttpStatus.BAD_REQUEST, errors);
    }

    public BadRequestException(String message) {
        super(message, HttpStatus.BAD_REQUEST);
    }
}
