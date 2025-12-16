package edu.univ.erp.api.common;

import java.util.Objects;
import java.util.Optional;

public final class ApiResponse<T> {

    private final boolean success;
    private final T payload;
    private final ErrorInfo error;

    private ApiResponse(boolean success, T payload, ErrorInfo error) {
        this.success = success;
        this.payload = payload;
        this.error = error;
    }

    public static <T> ApiResponse<T> success(T payload) {
        return new ApiResponse<>(true, payload, null);
    }

    public static <T> ApiResponse<T> failure(String code, String message) {
        return new ApiResponse<>(false, null, new ErrorInfo(code, message));
    }

    public boolean success() {
        return success;
    }

    public Optional<T> payload() {
        return Optional.ofNullable(payload);
    }

    public Optional<ErrorInfo> error() {
        return Optional.ofNullable(error);
    }

    @Override
    public String toString() {
        return "ApiResponse{" +
                "success=" + success +
                ", payload=" + payload +
                ", error=" + error +
                '}';
    }

    public record ErrorInfo(String code, String message) {
        public ErrorInfo {
            Objects.requireNonNull(code, "code");
            Objects.requireNonNull(message, "message");
        }
    }
}

