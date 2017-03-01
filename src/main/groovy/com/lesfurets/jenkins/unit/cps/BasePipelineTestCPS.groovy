package com.lesfurets.jenkins.unit.cps

import com.lesfurets.jenkins.unit.BasePipelineTest

class BasePipelineTestCPS extends BasePipelineTest {

    BasePipelineTestCPS() {
        helper = new PipelineTestHelperCPS()
        helper.setScriptRoots scriptRoots
        helper.setScriptExtension scriptExtension
        helper.setBaseClassloader this.class.classLoader
        helper.imports += imports
        helper.setBaseScriptRoot baseScriptRoot
    }
}
