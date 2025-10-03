package net.m9studio.springrelay.exception;

public class ConfigPathNotFoundException extends RuntimeException {
    public ConfigPathNotFoundException(String path) {
        super("Config path does not exist or is not a directory: " + path);
    }
}
