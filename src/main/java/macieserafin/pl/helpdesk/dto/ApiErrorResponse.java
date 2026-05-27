package macieserafin.pl.helpdesk.dto;

import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;

import java.time.Instant;
import java.util.List;

public record ApiErrorResponse(
        Instant timestamp,
        int status,
        String error,
        String message,
        String path,
        List<FieldError> errors
) {
    public ApiErrorResponse {
        errors = errors == null ? List.of() : List.copyOf(errors);
    }

    public static ApiErrorResponse of(HttpStatusCode statusCode, String message, String path) {
        HttpStatus status = HttpStatus.valueOf(statusCode.value());

        return new ApiErrorResponse(
                Instant.now(),
                status.value(),
                status.getReasonPhrase(),
                message,
                path,
                List.of()
        );
    }

    public static ApiErrorResponse validation(String message, String path, List<FieldError> errors) {
        return new ApiErrorResponse(
                Instant.now(),
                HttpStatus.BAD_REQUEST.value(),
                HttpStatus.BAD_REQUEST.getReasonPhrase(),
                message,
                path,
                errors
        );
    }

    public record FieldError(String field, String message) {
    }
}
