package com.keroz.beancopyutils.exception;

public class TypeMismatchException extends RuntimeException {

    /**
     *
     */
    private static final long serialVersionUID = 1L;
    
    public TypeMismatchException(Class<?> expected, Class<?> got) {
        super("Expected type of " + expected.getName() + ", but got: " + got.getName());
    }

}