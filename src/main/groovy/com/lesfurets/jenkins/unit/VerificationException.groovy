package com.lesfurets.jenkins.unit

/**
 * Exception thrown when verification in BasePipelineTest fails.
 */
class VerificationException extends RuntimeException {

    /**
     * Constructs a new VerificationException with the specified detail message.
     *
     * @param message the detail message
     */
    VerificationException(String message) {
        super(message)
    }

    /**
     * Constructs a new VerificationException with the specified detail message and cause.
     *
     * @param message the detail message
     * @param cause the cause of the exception
     */
    VerificationException(String message, Throwable cause) {
        super(message, cause)
    }
}
