package com.myorg.balancecalculator.exception;

public class FilePathMissingException extends RuntimeException{

    public FilePathMissingException(String message) {
        super(message);
    }

    public FilePathMissingException() {
    }
}
