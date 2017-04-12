package com.lesfurets.jenkins

import org.jenkinsci.plugins.docker.workflow.DockerDSL
import org.junit.Before
import org.junit.Test

import com.lesfurets.jenkins.unit.BasePipelineTest

class TestDockerDSL extends BasePipelineTest {

    @Override
    @Before
    void setUp() throws Exception {
        scriptRoots += 'src/test/jenkins'
        super.setUp()
        binding.setVariable('scm', [
                $class                           : 'GitSCM',
                branches                         : [[name: 'master']]
        ])
        binding.setVariable('env', [
                DOCKER_REGISTRY_URL: 'https://hub.docker.com/registry'
        ])
        helper.registerAllowedMethod('withDockerContainer', [Map.class, Closure.class], null)
    }

    @Test
    void should_execute_without_errors() throws Exception {
        def script = loadScript("job/dockerJob.jenkins")
        def docker = new DockerDSL().getValue(script)
        binding.setVariable('docker', docker)
        script.execute()
        printCallStack()
        assertJobStatusSuccess()
    }
}
