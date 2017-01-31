package com.lesfurets.jenkins.helpers.cps

import com.lesfurets.jenkins.helpers.BasePipelineTest

class BasePipelineTestCPS extends BasePipelineTest {

    BasePipelineTestCPS() {
        helper = new PipelineTestHelperCPS()
        helper.setScriptRoots roots
        helper.setScriptExtension extension
        helper.setBaseClassloader this.class.classLoader
        helper.setImports imports
        helper.setBaseScriptRoot baseScriptRoot
    }
}
