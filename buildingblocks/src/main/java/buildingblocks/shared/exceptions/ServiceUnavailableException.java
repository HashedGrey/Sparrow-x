package buildingblocks.shared.exceptions;

import org.springframework.http.HttpStatus;

public class ServiceUnavailableException extends CustomException {
    public ServiceUnavailableException(String message, Exception e) {
        super(message, HttpStatus.SERVICE_UNAVAILABLE);
    }
}