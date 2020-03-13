package org.example.testsharedlib

import com.lesfurets.jenkins.unit.cps.BasePipelineTestCPS

import org.example.testsharedlib.ModPipelineTestHelperCPS

abstract class ModBasePipelineTestCPS extends BasePipelineTestCPS {
  ModBasePipelineTestCPS() {
    helper = new ModPipelineTestHelperCPS()
  }
}
