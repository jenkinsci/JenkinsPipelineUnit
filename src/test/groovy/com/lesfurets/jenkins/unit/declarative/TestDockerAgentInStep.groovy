package com.lesfurets.jenkins.unit.declarative

import org.junit.Before
import org.junit.Test

class TestDockerAgentInStep extends DeclarativePipelineTest {

    @Before
    @Override
    void setUp() throws Exception {
        scriptRoots += 'src/test/jenkins/jenkinsfiles'
        scriptExtension = ''
        super.setUp()
    }

    @Test
    void test_example() {
        runScript("Docker_agentInStep_JenkinsFile")
        assertJobStatusSuccess()
    }

    @Test
    void test_dockerfile_agent() {
        runScript("Dockerfile_agent_JenkinsFile")
        assertJobStatusSuccess()
    }
}
