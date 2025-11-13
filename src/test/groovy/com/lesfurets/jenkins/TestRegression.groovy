package com.lesfurets.jenkins

import com.lesfurets.jenkins.unit.cps.BaseRegressionTestCPS
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class TestRegression extends BaseRegressionTestCPS {

    @Override
    @BeforeEach
    void setUp() throws Exception {
        scriptRoots += 'src/test/jenkins'
        super.setUp()
        def scmBranch = "feature_test"
        helper.registerAllowedMethod("sh", [Map.class], {c -> 'bcc19744'})
        binding.setVariable('scm', [
                        $class                           : 'GitSCM',
                        branches                         : [[name: scmBranch]],
                        extensions                       : [],
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
