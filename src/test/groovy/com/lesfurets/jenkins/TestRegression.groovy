package com.lesfurets.jenkins

import org.junit.Before
import org.junit.Test

import com.lesfurets.jenkins.unit.BaseRegressionTest

class TestRegression extends BaseRegressionTest {

    @Override
    @Before
    void setUp() throws Exception {
        super.setUp()
        def scmBranch = 'feature_test'
        helper.registerAllowedMethod('sh', [Map.class], {c -> 'bcc19744'})
        binding.setVariable('scm', [
                        $class                           : 'GitSCM',
                        branches                         : [[name: scmBranch]],
                        doGenerateSubmoduleConfigurations: false,
                        extensions                       : [],
                        submoduleCfg                     : [],
                        userRemoteConfigs                : [[
                                                                            credentialsId: 'gitlab_git_ssh',
                                                                            url          : 'github.com/lesfurets/pipeline-test-helper.git'
                                                            ]]
        ])
    }

    @Test
    void testNonReg() throws Exception {
        def script = loadScript('job/exampleJob.jenkins')
        script.execute()
        super.testNonRegression('example', false)
    }

}
