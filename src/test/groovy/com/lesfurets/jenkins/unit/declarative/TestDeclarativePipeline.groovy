package com.lesfurets.jenkins.unit.declarative

import static org.assertj.core.api.Assertions.*

import org.junit.Before
import org.junit.Test

class TestDeclarativePipeline extends DeclarativePipelineTest {

    @Before
    @Override
    void setUp() throws Exception {
        scriptRoots = ['src/test/jenkins/jenkinsfiles']
        scriptExtension = ''
        super.setUp()
    }

    @Test void jenkinsfile_success() throws Exception {
        def script = runScript('Declarative_Jenkinsfile')
        printCallStack()
        assertJobStatusSuccess()
        assertCallStackContains('pipeline unit tests PASSED')
        assertCallStackContains('pipeline unit tests completed')
    }

    @Test void jenkinsfile_failure() throws Exception {
        helper.registerAllowedMethod('sh', [String.class], { String cmd ->
            updateBuildStatus('FAILURE')
        })
        runScript('Declarative_Jenkinsfile')
        printCallStack()
        assertJobStatusFailure()
        assertCallStack()
        assertCallStack().contains('pipeline unit tests FAILED')
        assertCallStackContains('pipeline unit tests completed')
    }

    @Test void should_params() throws Exception {
        runScript('Params_Jenkinsfile')
        printCallStack()
        assertCallStack().contains('Hello Mr Jenkins')
    }

    @Test void when_branch() throws Exception {
        addEnvVar('BRANCH_NAME', 'production')
        runScript('Branch_Jenkinsfile')
        printCallStack()
        assertCallStack().contains('Deploying')
        assertJobStatusSuccess()
    }

    @Test void when_branch_not() throws Exception {
        addEnvVar('BRANCH_NAME', 'master')
        runScript('Branch_Jenkinsfile')
        printCallStack()
        assertCallStack().contains('Skipping stage Example Deploy')
        assertJobStatusSuccess()
    }

    @Test void should_agent() throws Exception {
        runScript('Agent_Jenkinsfile')
        printCallStack()
        assertJobStatusSuccess()
        assertCallStack().contains('openjdk:8-jre')
        assertCallStack().contains('maven:3-alpine')
    }

    @Test void should_credentials() throws Exception {
        addCredential('my-prefined-secret-text', 'something_secret')
        runScript('Credentials_Jenkinsfile')
        printCallStack()
        assertJobStatusSuccess()
        assertCallStack().contains('AN_ACCESS_KEY:something_secret')
    }

    @Test void should_parallel() throws Exception {
        runScript('Parallel_Jenkinsfile')
        printCallStack()
        assertJobStatusSuccess()
    }

    @Test void should_sub_stages() throws Exception {
        runScript('ComplexStages_Jenkinsfile')
        printCallStack()
        assertJobStatusSuccess()
    }

    @Test(expected = MissingPropertyException)
    void should_non_valid_fail() throws Exception {
        try {
            runScript('Non_Valid_Jenkinsfile')
        } catch (e) {
            e.printStackTrace()
            throw e
        } finally {
            printCallStack()
        }
    }
}
