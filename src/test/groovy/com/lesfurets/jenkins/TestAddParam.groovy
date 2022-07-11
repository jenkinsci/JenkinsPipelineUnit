package com.lesfurets.jenkins

import com.lesfurets.jenkins.unit.BasePipelineTest
import org.junit.Test

class TestAddParam extends BasePipelineTest {

    @Test
    void readUndefinedParamNoException() throws Exception {
        super.setUp()
        addParam('FOO', 'BAR')
    }

    @Test(expected = UnsupportedOperationException)
    void addParamImmutable() throws Exception {
        super.setUp()
        addParam('FOO', 'BAR')

        // We should not be able to modify existing parameters. This would not work on Jenkins.
        binding.getVariable('params')['FOO'] = 'NOT-BAR'
    }

    @Test(expected = UnsupportedOperationException)
    void addNewParamImmutable() throws Exception {
        super.setUp()

        // It also is not permitted to add new parameters directly. Instead, addParam must be used.
        binding.getVariable('params')['BAZ'] = 'QUX'
    }
}
