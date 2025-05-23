package org.example.exceptions;

public class JogadorNotFoundException extends RuntimeException {
    public JogadorNotFoundException(String message) {
        super(message);
    }
}
