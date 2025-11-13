package com.lesfurets.jenkins

import com.lesfurets.jenkins.unit.BasePipelineTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class TestSerialization extends BasePipelineTest {

    @Override
    @BeforeEach
    void setUp() throws Exception {
        scriptRoots += 'src/test/jenkins'
        super.setUp()
        def scmBranch = "feature_test"
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

    /**
     * This exception should have thrown an exception because of the bad usage of NonCPS method.
     * @throws Exception
     */
    @Test
//                    (expected = Exception.class)
    void testException() throws Exception {
        def script = runScript('job/serialize.jenkins')
        try {
            script.execute()
        } catch (e) {
            throw e
        } finally {
            printCallStack()
        }
    }

}
