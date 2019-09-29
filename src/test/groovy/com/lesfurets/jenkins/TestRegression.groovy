package com.lesfurets.jenkins

import com.lesfurets.jenkins.unit.MethodSignature
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

        helper.registerMockForMethod(new MethodSignature('sh', Map), { String rule, Map shArgs -> shArgs.script =~ rule })
                .mockWithString('git rev-parse HEAD', 'bcc19744')
                .mockWithString('whoami', 'jenkins')
        // And we can later do:
        helper.getMock('sh', Map).mockWithFile('ls', this.class, 'ls.txt')
        helper.getMock('sh', Map).mockWithClosure('^echo ', {args -> println args.script[5..-1]})

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
