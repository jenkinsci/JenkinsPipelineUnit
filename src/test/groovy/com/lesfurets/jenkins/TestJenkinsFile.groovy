package com.lesfurets.jenkins

import static org.assertj.core.api.Assertions.assertThat

import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized

import com.lesfurets.jenkins.helpers.BasePipelineTest
import com.lesfurets.jenkins.helpers.MethodCall

@RunWith(Parameterized.class)
class TestJenkinsFile extends BasePipelineTest {

    @Parameterized.Parameter
    public String branch
    @Parameterized.Parameter(1)
    public String expectedPhase

    @Parameterized.Parameters(name = "Test branch {0} phase {1}")
    static Collection<Object[]> data() {
        return [['develop', 'deploy'],
                ['master', 'deploy'],
                ['feature_', 'verify']
        ].collect { it as Object[] }
    }

    @Override
    @Before
    void setUp() throws Exception {
        def scmBranch = branch
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
        helper.registerAllowedMethod("file", [Map.class], stringInterceptor)
        helper.registerAllowedMethod("archiveArtifacts", [String.class], null)
        super.setUp()
    }

    @Test
    void name() throws Exception {
        loadScript("Jenkinsfile")
        printCallStack()
        assertThat(helper.callStack.stream()
                        .filter { c -> c.methodName == "sh" }
                        .map(MethodCall.&callArgsToString)
                        .findAll { s -> s.contains("mvn clean $expectedPhase") })
                        .hasSize(1)
    }
}
