package com.lesfurets.jenkins.unit

abstract class BaseDeclarativePipelineTest extends BasePipelineTest {

    Script jenkinsfile = null

    DeclarativePipelineHarness harness


    @Override
    void setUp() {
        if(harness == null || jenkinsfile == null) {
            super.setUp()
            harness = new DeclarativePipelineHarness(this, allowedClosureSteps(), allowedParameterizedClosureSteps())
            loadJenkinsfile()
        } else {
            harness.clearAllCalls()
        }
    }

    abstract String jenkinsfileName()

    def loadJenkinsfile() {
        this.jenkinsfile = super.loadScript(jenkinsfileName())
    }

    List<String> defaultAllowedClosureSteps = ['pipeline',
                                               'options',
                                               'parameters',
                                               'environment',
                                               'triggers',
                                               'tools',
                                               'input',

                                               'agent',
                                               'docker',
                                               'dockerfile',
                                               'node',

                                               'stages',
                                               'parallel',
                                               'script',
                                               'steps',

                                               'when',
                                               'allOf',
                                               'anyOf',
                                               'expression',

                                               'post',
                                               'always',
                                               'cleanup',
                                               'success',
                                               'failure',
                                               'regression',
                                               'changed',
                                               'fixed',
                                               'aborted',
                                               'unstable',
                                               'unsuccessful']

    Map<String, Class<Object>> defaultAllowedParameterizedClosureSteps = [
            'stage': String.class as Class<Object>,
            'node' : String.class as Class<Object>,
            'withCredentials': Object.class,
            'withEnv': List.class as Class<Object>,
            'dir': String.class as Class<Object>,
    ]

    List<String> allowedClosureSteps() {
        return defaultAllowedClosureSteps
    }

    Map<String, Class<Object>> allowedParameterizedClosureSteps() {
        return defaultAllowedParameterizedClosureSteps
    }
}
