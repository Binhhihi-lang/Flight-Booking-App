package com.example.flight_booking_app.data.model;

/**
 * Wrapper class để truyền trạng thái từ Repository ViewModel View.
 */
public class UiState {

    public enum Status {
        LOADING, SUCCESS, ERROR
    }

    private final Status status;
    private final String message;

    private UiState(Status status, String message) {
        this.status = status;
        this.message = message;
    }

    public static UiState loading() {
        return new UiState(Status.LOADING, null);
    }

    public static UiState success() {
        return new UiState(Status.SUCCESS, null);
    }

    public static UiState error(String message) {
        return new UiState(Status.ERROR, message);
    }

    public Status getStatus() {
        return status;
    }

    public String getMessage() {
        return message;
    }
}
