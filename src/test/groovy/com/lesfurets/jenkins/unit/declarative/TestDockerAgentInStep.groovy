package com.lesfurets.jenkins.unit.declarative

import org.junit.Before
import org.junit.Test

class TestDockerAgentInStep extends DeclarativePipelineTest {

    @Before
    @Override
    void setUp() throws Exception {
        super.setUp()
    }

    @Test
    void test_example() {
        runScript("src/test/jenkins/jenkinsfiles/Docker_agentInStep_JenkinsFile")
        assertJobStatusSuccess()
    }
}
