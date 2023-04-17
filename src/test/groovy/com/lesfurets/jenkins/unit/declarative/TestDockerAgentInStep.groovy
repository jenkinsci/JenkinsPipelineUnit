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
    void test_dockerfile_agent_only_filename_specified() {
        runScript("Dockerfile_Agent_Only_Filename_JenkinsFile")
        assertCallStackContains('Executing on agent [dockerfile')
        assertJobStatusSuccess()
    }

    @Test void test_dockerfile_default_agent() throws Exception {
        runScript('Dockerfile_Agent_Default_Jenkinsfile')
        assertCallStackContains('Executing on agent [dockerfile')
        assertJobStatusSuccess()
    }

    @Test
    void test_docker_agent_callstack_does_not_contain_binding() {
        runScript("Docker_agentInStep_JenkinsFile")
        assertJobStatusSuccess()
        assertCallStack().doesNotContain('binding:groovy.lang.Binding@')
        assertCallStackContains('Docker_agentInStep_JenkinsFile.echo(Executing on agent [docker:[image:maven, reuseNode:false, stages:[:], args:, alwaysPull:true, containerPerStageRoot:false, label:latest]])')
    }
}
