package com.lesfurets.jenkins

import org.junit.Before
import org.junit.Test

import com.lesfurets.jenkins.unit.cps.BasePipelineTestCPS

class TestSerializationCPS extends BasePipelineTestCPS {



    @Override
    @Before
    void setUp() throws Exception {
        scriptRoots += 'src/test/jenkins'
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
                                                                            url          : 'github.com/lesfurets/JenkinsPipelineUnit.git'
                                                            ]]
        ])
    }

    @Test(expected = Exception.class)
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

    @Test
    void testSerialization() throws Exception {
        def script = runScript('job/serializeCPS.jenkins')
        try {
            script.execute()
        } catch (e) {
            throw e
        } finally {
            printCallStack()
        }
    }
}
