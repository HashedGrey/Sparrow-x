package buildingblocks.shared.exceptions;

import org.springframework.http.HttpStatus;

public class ForbiddenException extends CustomException {
    public ForbiddenException(String message) {
        super(message, HttpStatus.FORBIDDEN);
    }
}