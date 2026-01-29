package buildingblocks.shared.exceptions;

import lombok.Getter;
import org.springframework.http.HttpStatus;
import java.util.List;
import java.util.Map;

@Getter
public class HttpResponseException extends CustomException {

    private final String responseContent;
    private final Map<String, List<String>> headers;

    public HttpResponseException(String responseContent, HttpStatus status, Map<String, List<String>> headers) {
        super(responseContent, status, List.of(responseContent));
        this.responseContent = responseContent;
        this.headers = headers;
    }

}

