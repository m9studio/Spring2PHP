package net.m9studio.springrelay.exception;

public class InvalidEntryException extends RuntimeException {
    public InvalidEntryException(String reason) {
        super("Invalid relay entry: " + reason);
    }
}
