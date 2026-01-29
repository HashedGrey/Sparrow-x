package buildingblocks.shared.exceptions;

public class ErrorResponse {
    public final String message;
    public final int code;

    public ErrorResponse(String message, int code) {
        this.message = message;
        this.code = code;
    }
}