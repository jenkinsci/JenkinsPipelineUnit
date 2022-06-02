package com.lesfurets.jenkins.unit

import org.junit.Before
import org.junit.Test


/**
 * Test the DockerMock class
 *
 * It would be a bit redundant to write regular unit tests for DockerMock, because most of
 * the methods are empty or void. So instead this test class loads a Jenkins pipeline file
 * which exercises all of the methods of the docker singleton.
 */
class DockerMockTest extends BasePipelineTest {
  Object script = null

  @Before
  @Override
  void setUp() {
    super.setUp()
  }

  @Test
  void executePipeline() throws Exception {
    runScript('src/test/jenkins/jenkinsfiles/Docker_Jenkinsfile')
    printCallStack()
  }
}
