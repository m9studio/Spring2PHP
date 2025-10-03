package net.m9studio.springrelay.exception;

public class MultipleMatchesException extends RuntimeException {
    public MultipleMatchesException(String path, String method, int size) {
        super("Multiple relay entries matched for path='" + path + "', method='" + method + "' (count=" + size + ")");
    }
}
