package com.lesfurets.jenkins

import com.lesfurets.jenkins.unit.BasePipelineTest
import org.junit.jupiter.api.Test

class TestAddEnvVar extends BasePipelineTest {

    @Test
    void readUndefinedParamNoException() throws Exception {
        super.setUp()
        addEnvVar('FOO', 'BAR')
    }
}
