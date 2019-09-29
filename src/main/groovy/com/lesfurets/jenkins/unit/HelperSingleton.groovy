package com.lesfurets.jenkins.unit

class HelperSingleton {

    static PipelineTestHelper singletonInstance;

    static void setSingletonInstance(PipelineTestHelper helper) {
        singletonInstance = helper
    }

    static PipelineTestHelper getSingletonInstance() {

        if (singletonInstance != null) {
            return singletonInstance
        }

        return new PipelineTestHelper()
    }

    static void invalidate() {
        setSingletonInstance(null)
    }
}
