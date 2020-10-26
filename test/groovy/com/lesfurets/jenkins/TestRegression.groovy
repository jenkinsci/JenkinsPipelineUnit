package com.lesfurets.jenkins

import org.junit.Before
import org.junit.Test

import com.lesfurets.jenkins.unit.cps.BaseRegressionTestCPS

class TestRegression extends BaseRegressionTestCPS {

    @Override
    @Before
    void setUp() throws Exception {
        scriptRoots += 'src/test/jenkins'
        super.setUp()
        def scmBranch = "feature_test"
        helper.registerAllowedMethod("sh", [Map.class], {c -> 'bcc19744'})
        binding.setVariable('scm', [
                        $class                           : 'GitSCM',
                        branches                         : [[name: scmBranch]],
                        doGenerateSubmoduleConfigurations: false,
                        extensions                       : [],
                        submoduleCfg                     : [],
                        userRemoteConfigs                : [[
                                                                            credentialsId: 'gitlab_git_ssh',
                                                                            url          : 'github.com/lesfurets/JenkinsPipelineUnit.git'
                                                            ]]
        ])
    }

    @Test
    void testNonReg() throws Exception {
        def script = runScript("job/exampleJob.jenkins")
        script.execute()
        super.testNonRegression("example")
    }

}
