package com.lesfurets.jenkins

import com.lesfurets.jenkins.unit.BasePipelineTest
import org.junit.Test

class TestAddCredential extends BasePipelineTest {

    @Test
    void readUndefinedParamNoException() throws Exception {
        super.setUp()
        addCredential('FOO', 'BAR')
    }
}
