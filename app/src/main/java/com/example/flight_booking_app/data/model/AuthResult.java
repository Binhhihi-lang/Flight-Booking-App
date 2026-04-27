package com.example.flight_booking_app.data.model;

/**
 * Wrapper class để truyền trạng thái từ Repository ViewModel View.
 */
public class AuthResult {

    public enum Status {
        LOADING, SUCCESS, ERROR
    }

    private final Status status;
    private final String message;

    private AuthResult(Status status, String message) {
        this.status = status;
        this.message = message;
    }

    public static AuthResult loading() {
        return new AuthResult(Status.LOADING, null);
    }

    public static AuthResult success() {
        return new AuthResult(Status.SUCCESS, null);
    }

    public static AuthResult error(String message) {
        return new AuthResult(Status.ERROR, message);
    }

    public Status getStatus() { return status; }
    public String getMessage() { return message; }
}
