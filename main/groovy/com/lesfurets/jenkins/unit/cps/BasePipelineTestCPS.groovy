package com.lesfurets.jenkins.unit.cps

import com.lesfurets.jenkins.unit.BasePipelineTest
import com.lesfurets.jenkins.unit.PipelineTestHelper

class BasePipelineTestCPS extends BasePipelineTest {

    BasePipelineTestCPS(PipelineTestHelper helper) {
        super(helper)
    }

    BasePipelineTestCPS() {
        super(new PipelineTestHelperCPS())
    }
}
