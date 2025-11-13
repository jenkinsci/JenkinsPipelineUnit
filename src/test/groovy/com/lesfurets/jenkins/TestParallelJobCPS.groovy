package com.lesfurets.jenkins

import com.lesfurets.jenkins.unit.cps.BasePipelineTestCPS
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class TestParallelJobCPS extends BasePipelineTestCPS {

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
    void should_execute_parallel_with_errors() throws Exception {
        def script = runScript("job/parallelJob.jenkins")
        try{
            script.execute()
        } finally {
            printCallStack()
        }
        assertJobStatusFailure()
    }
}
