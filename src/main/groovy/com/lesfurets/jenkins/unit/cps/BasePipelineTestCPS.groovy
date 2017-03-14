package com.lesfurets.jenkins.unit.cps

import com.lesfurets.jenkins.unit.BasePipelineTest

class BasePipelineTestCPS extends BasePipelineTest {

    BasePipelineTestCPS() {
        helper = new PipelineTestHelperCPS()
    }
}
