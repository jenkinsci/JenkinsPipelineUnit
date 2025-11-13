package com.lesfurets.jenkins

import com.lesfurets.jenkins.unit.BaseRegressionTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class TestParametersJob extends BaseRegressionTest {

  @Override
  @BeforeEach
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
