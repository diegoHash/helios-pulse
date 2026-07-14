package com.helios.platform.pulse.exceptions;

public class OwnerNotFoundException extends Exception {
    public OwnerNotFoundException(String message) {
        super(message);
    }

    public OwnerNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    public OwnerNotFoundException(Throwable cause) {
        super(cause);
    }

    public OwnerNotFoundException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

}
