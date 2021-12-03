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

    @Test
    void test_dockerfile_agent_callstack_does_not_contain_binding() {
        runScript("Docker_agentInStep_JenkinsFile")
        assertJobStatusSuccess()
        assertCallStack().doesNotContain('binding:groovy.lang.Binding@')
        assertCallStack().contains('Docker_agentInStep_JenkinsFile.echo(Executing on agent [docker:[image:maven, reuseNode:false, stages:[:], args:, alwaysPull:true, containerPerStageRoot:false, label:latest]])')
    }
}
