package com.lesfurets.jenkins

import com.lesfurets.jenkins.unit.BaseRegressionTest
import org.junit.Before
import org.junit.Test

class TestWithCredentialsAndParametersJob extends BaseRegressionTest {

  @Override
  @Before
  void setUp() throws Exception {
    scriptRoots += 'src/test/jenkins'
    super.setUp()
  }

  @Test
  void should_run_script_with_parameters() {
      // given:
      addStringCredential('my-gitlab-api-token', 'gitlab-api-token')

    // when:
    runScript("job/withCredentialsAndParameters.jenkins")

    // then:
    assertJobStatusSuccess()
    testNonRegression("withCredentialsAndParameters")
  }
}
