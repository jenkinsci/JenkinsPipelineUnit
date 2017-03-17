package com.lesfurets.jenkins

import org.junit.Before
import org.junit.Test

import com.lesfurets.jenkins.unit.BaseRegressionTest
import com.lesfurets.jenkins.unit.RegressionTestHelper

class TestRegression extends BaseRegressionTest {

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

    @Test(expected = AssertionError)
    void testNonReg_true() throws Exception {
        def script = loadScript("job/exampleJob.jenkins")
        script.execute()
        System.setProperty(RegressionTestHelper.WRITE_STACKS_PROPERTY, 'true')
        super.testNonRegression("example_true")
    }

    @Test
    void testNonReg_false() throws Exception {
        def script = loadScript("job/exampleJob.jenkins")
        script.execute()
        System.setProperty(RegressionTestHelper.WRITE_STACKS_PROPERTY, 'false')
        super.testNonRegression("example_false")
    }

    @Test
    void testNonReg_null() throws Exception {
        def script = loadScript("job/exampleJob.jenkins")
        script.execute()
        System.setProperty(RegressionTestHelper.WRITE_STACKS_PROPERTY, '')
        super.testNonRegression("example_null")
    }

}
