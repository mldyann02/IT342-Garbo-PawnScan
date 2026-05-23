package edu.cit.garbo.pawnscan.features.auth.exception;

public class InvalidGoogleTokenException extends RuntimeException {

    public InvalidGoogleTokenException(String message) {
        super(message);
    }

    public InvalidGoogleTokenException(String message, Throwable cause) {
        super(message, cause);
    }
}
