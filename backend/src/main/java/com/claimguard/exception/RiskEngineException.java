package com.claimguard.exception;

public class RiskEngineException extends RuntimeException {
    public RiskEngineException(String message) {
        super(message);
    }

    public RiskEngineException(String message, Throwable cause) {
        super(message, cause);
    }
}
