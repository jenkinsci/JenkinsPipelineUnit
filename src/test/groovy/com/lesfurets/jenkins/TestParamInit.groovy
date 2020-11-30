package com.lesfurets.jenkins

import com.lesfurets.jenkins.unit.BasePipelineTest
import org.junit.Test

class TestParamInit extends BasePipelineTest {

    @Test
    void readUndefinedParamNoException() throws Exception {
        scriptRoots += 'src/test/jenkins'
        super.setUp()
        runScript('job/printParams.jenkins')
    }
}
