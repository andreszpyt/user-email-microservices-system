package dev.user.controller.exceptions;

public class CredentialAlreadyExistsException extends RuntimeException {
    public CredentialAlreadyExistsException(String message) {
        super(message);
    }
}
