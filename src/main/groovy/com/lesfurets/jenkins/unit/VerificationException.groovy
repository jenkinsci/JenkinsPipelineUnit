package com.lesfurets.jenkins.unit

/**
 * Custom exception class used in the verify method in BasePipelineTest
 */
class VerificationException extends Exception {

    VerificationException(String errorMessage) {
        super(errorMessage)
    }
}
