package com.lesfurets.jenkins

import com.lesfurets.jenkins.unit.BasePipelineTest
import org.junit.jupiter.api.Test

import static org.junit.jupiter.api.Assertions.assertThrows

class TestAddParam extends BasePipelineTest {

    @Test
    void readUndefinedParamNoException() throws Exception {
        super.setUp()
        addParam('FOO', 'BAR')
    }

    @Test
    void addParamImmutable() throws Exception {
        super.setUp()
        addParam('FOO', 'BAR')

        assertThrows(UnsupportedOperationException, { ->
            // We should not be able to modify existing parameters. This would not work on Jenkins.
            binding.getVariable('params')['FOO'] = 'NOT-BAR'
        })
    }

    @Test
    void addNewParamImmutable() throws Exception {
        super.setUp()

        assertThrows(UnsupportedOperationException, { ->
            // It also is not permitted to add new parameters directly. Instead, addParam must be used.
            binding.getVariable('params')['BAZ'] = 'QUX'
        })
    }
}
