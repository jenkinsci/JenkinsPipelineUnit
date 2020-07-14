package com.lesfurets.jenkins

import com.lesfurets.jenkins.unit.BaseRegressionTest
import org.junit.Before
import org.junit.Test

import static org.junit.Assert.assertTrue

class TestWithCredentialsJob extends BaseRegressionTest {

    @Override
    @Before
    void setUp() throws Exception {
        scriptRoots += 'src/test/jenkins'
        super.setUp()
    }

    @Test
    void should_run_script_with_credentials() {
        // when:
        runScript("job/withCredentials.jenkins")

        // then:
        assertJobStatusSuccess()
        testNonRegression("withCredentials")
    }
}
