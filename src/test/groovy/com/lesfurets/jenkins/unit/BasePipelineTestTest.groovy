package com.lesfurets.jenkins.unit

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

import static org.assertj.core.api.Assertions.assertThat

class BasePipelineTestTest extends BasePipelineTest {
    
    @BeforeEach
    @Override
    void setUp() throws Exception {
        scriptRoots += 'src/test/jenkins/jenkinsfiles'
        scriptExtension = ''
        super.setUp()
    }

    @Test
    void buildStatusFailure() {
        runScript('BuildStatus_Failure_Jenkinsfile')
        assertThat(binding.getVariable('currentBuild').result).isEqualTo('FAILURE')
        assertThat(binding.getVariable('currentBuild').currentResult).isEqualTo('FAILURE')
    }

    @Test
    void buildStatusSuccess() {
        runScript('BuildStatus_Success_Jenkinsfile')
        assertThat(binding.getVariable('currentBuild').result).isEqualTo('SUCCESS')
        assertThat(binding.getVariable('currentBuild').currentResult).isEqualTo('SUCCESS')
    }
}
