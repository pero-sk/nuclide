package com.penguin.nuclide.nowns.validation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class ValidationResult {
    private final List<ValidationError> errors = new ArrayList<>();

    public void addError(String code, String message) {
        errors.add(new ValidationError(code, message));
    }

    public void addError(ValidationError error) {
        errors.add(error);
    }

    public boolean isValid() {
        return errors.isEmpty();
    }

    public List<ValidationError> errors() {
        return Collections.unmodifiableList(errors);
    }

    public String formatErrors() {
        if (errors.isEmpty()) {
            return "No validation errors";
        }

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < errors.size(); i++) {
            ValidationError error = errors.get(i);
            if (i > 0) sb.append(System.lineSeparator());
            sb.append("- [")
              .append(error.code())
              .append("] ")
              .append(error.message());
        }
        return sb.toString();
    }
}