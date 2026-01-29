package buildingblocks.shared.exceptions;

import java.util.List;

public class ValidationException extends BadRequestException {

    public ValidationException(String message, List<String> errors) {
        super(message, errors);
    }

    public ValidationException(String message) {
        super(message);
    }
}

