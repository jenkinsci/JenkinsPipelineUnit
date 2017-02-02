package com.lesfurets.jenkins

import org.junit.Before
import org.junit.Test

import com.lesfurets.jenkins.unit.BasePipelineTest

class TestSerialization extends BasePipelineTest {

    @Override
    @Before
    void setUp() throws Exception {
        super.setUp()
        def scmBranch = "feature_test"
        binding.setVariable('scm', [
                        $class                           : 'GitSCM',
                        branches                         : [[name: scmBranch]],
                        doGenerateSubmoduleConfigurations: false,
                        extensions                       : [],
                        submoduleCfg                     : [],
                        userRemoteConfigs                : [[
                                                                            credentialsId: 'gitlab_git_ssh',
                                                                            url          : "github.com/lesfurets/pipeline-test-helper.git"
                                                            ]]
        ])
    }

    /**
     * This exception should have thrown an exception because of the bad usage of NonCPS method.
     * @throws Exception
     */
    @Test
//                    (expected = Exception.class)
    void testException() throws Exception {
        def script = loadScript('job/serialize.jenkins')
        try {
            script.execute()
        } catch (e) {
            throw e
        } finally {
            printCallStack()
        }
    }

}
