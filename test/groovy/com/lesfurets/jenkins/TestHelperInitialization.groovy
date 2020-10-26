package com.lesfurets.jenkins

import org.junit.Test

import com.lesfurets.jenkins.unit.BasePipelineTest

class TestHelperInitialization extends BasePipelineTest {

    @Test(expected = IllegalStateException)
    void non_initialized_helper() throws Exception {
        runScript('jobs/exampleJob.jenkins')
    }

    @Test(expected = NullPointerException)
    void non_initialized_gse() throws Exception {
        helper.loadScript('jobs/exampleJob.jenkins')
    }

    @Test
    void initialized_helper() throws Exception {
        scriptRoots += 'src/test/jenkins'
        super.setUp()
        helper.loadScript('job/exampleJob.jenkins')
    }
}
