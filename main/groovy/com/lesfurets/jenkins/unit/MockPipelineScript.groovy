package com.lesfurets.jenkins.unit

abstract class MockPipelineScript extends Script {

    /**
     * Override sleep method
     */
    void sleep(long milliseconds) {
        // no-op
    }
}
