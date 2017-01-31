package com.lesfurets.jenkins

import com.lesfurets.jenkins.helpers.BasePipelineTest
import org.junit.Before
import org.junit.Test

import static org.assertj.core.api.Assertions.assertThat

class TestParallelJob extends BasePipelineTest {

    @Override
    @Before
    void setUp() throws Exception {
        helper.baseScriptRoot = ""
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
        super.setUp()
    }

    @Test
    void name() throws Exception {
        def script = loadScript("job/parallelJob.jenkins")
        script.execute()
        printCallStack()

        assertJobStatusFailure()

    }
}
