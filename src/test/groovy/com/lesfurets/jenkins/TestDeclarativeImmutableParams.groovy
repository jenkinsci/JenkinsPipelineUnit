package com.lesfurets.jenkins

import com.lesfurets.jenkins.unit.declarative.DeclarativePipelineTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

import static org.junit.jupiter.api.Assertions.assertEquals
import static org.junit.jupiter.api.Assertions.assertThrows

class TestDeclarativeImmutableParams extends DeclarativePipelineTest {

    @Override
    @BeforeEach
    void setUp() throws Exception {
        scriptRoots += 'src/test/jenkins'

        super.setUp()
    }

    @Test
    void "test immutable params in declarative pipeline"() {
        assertThrows(UnsupportedOperationException, { ->
            runScript("job/library/test_params_immutable_declarative.jenkins")
        })
        assertEquals('null', binding.params['new'].toString())
    }
}
