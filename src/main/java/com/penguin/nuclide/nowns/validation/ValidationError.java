package com.penguin.nuclide.nowns.validation;

import java.util.Objects;

public final class ValidationError {
    private final String code;
    private final String message;

    public ValidationError(String code, String message) {
        this.code = Objects.requireNonNull(code);
        this.message = Objects.requireNonNull(message);
    }

    public String code() {
        return code;
    }

    public String message() {
        return message;
    }

    @Override
    public String toString() {
        return "ValidationError{" +
                "code='" + code + '\'' +
                ", message='" + message + '\'' +
                '}';
    }
}