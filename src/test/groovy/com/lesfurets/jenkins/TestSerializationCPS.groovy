package com.lesfurets.jenkins

import com.lesfurets.jenkins.unit.cps.BasePipelineTestCPS
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

import static org.junit.jupiter.api.Assertions.assertThrows

class TestSerializationCPS extends BasePipelineTestCPS {



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

    @Test
    void testException() throws Exception {
        assertThrows(Exception.class, { ->
            def  script = loadScript('job/serialize.jenkins')
            try {
                script.execute()
            } catch ( e) {
                throw e
            } finally {
                printCallStack()
            }
        })
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
