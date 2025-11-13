package com.lesfurets.jenkins

import com.lesfurets.jenkins.unit.BasePipelineTest
import org.junit.jupiter.api.Test

import static org.junit.jupiter.api.Assertions.assertThrows

class TestHelperInitialization extends BasePipelineTest {

    @Test
    void non_initialized_helper() throws Exception {
        assertThrows(IllegalStateException, { ->
            runScript('jobs/exampleJob.jenkins')
        })
    }

    @Test
    void non_initialized_gse() throws Exception {
        assertThrows(NullPointerException, { ->
            helper.loadScript('jobs/exampleJob.jenkins')
        })
    }

    @Test
    void initialized_helper() throws Exception {
        scriptRoots += 'src/test/jenkins'
        super.setUp()
        helper.loadScript('job/exampleJob.jenkins')
    }
}
