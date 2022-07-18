package com.lesfurets.jenkins

import com.lesfurets.jenkins.unit.declarative.DeclarativePipelineTest
import org.junit.Before
import org.junit.Ignore
import org.junit.Test

import static org.junit.Assert.assertEquals

class TestDeclarativeImmutableParams extends DeclarativePipelineTest {

    @Override
    @Before
    void setUp() throws Exception {
        scriptRoots += 'src/test/jenkins'

        super.setUp()
    }

    @Ignore
    @Test(expected = UnsupportedOperationException)
    void "test immutable params in declarative pipeline"() {
        runScript("job/library/test_params_immutable_declarative.jenkins")
        assertEquals('original', binding.params['new'].toString())
    }
}
