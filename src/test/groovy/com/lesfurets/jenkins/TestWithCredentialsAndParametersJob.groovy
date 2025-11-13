package com.lesfurets.jenkins

import com.lesfurets.jenkins.unit.BaseRegressionTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class TestWithCredentialsAndParametersJob extends BaseRegressionTest {

  @Override
  @BeforeEach
  void setUp() throws Exception {
    scriptRoots += 'src/test/jenkins'
    super.setUp()
  }

  @Test
  void should_run_script_with_parameters() {
    // when:
    runScript("job/withCredentialsAndParameters.jenkins")

    // then:
    assertJobStatusSuccess()
    testNonRegression("withCredentialsAndParameters")
  }
}
