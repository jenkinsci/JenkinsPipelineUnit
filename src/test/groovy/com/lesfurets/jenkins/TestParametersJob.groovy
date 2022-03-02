package com.lesfurets.jenkins

import com.lesfurets.jenkins.unit.BaseRegressionTest
import org.junit.Before
import org.junit.Test

class TestParametersJob extends BaseRegressionTest {

  @Override
  @Before
  void setUp() throws Exception {
    scriptRoots += 'src/test/jenkins'
    super.setUp()
  }

  @Test
  void should_run_script_parameters() {
    // when:
    runScript("job/parameters.jenkins")

    // then:
    assertJobStatusSuccess()
    testNonRegression("parameters")
  }
}
