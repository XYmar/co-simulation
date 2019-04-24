package com.rengu.cosimulation.exception;

/**
 * Author: XYmar
 * Date: 2019/4/18 15:46
 */
public class SpecificationException extends RuntimeException {
    public SpecificationException() {
    }

    public SpecificationException(String string) {
        super(string);
    }

    public SpecificationException(String message, Throwable cause) {
        super(message, cause);
    }

    public SpecificationException(Throwable cause) {
        super(cause);
    }

}
