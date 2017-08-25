package com.lesfurets.jenkins

import static org.assertj.core.api.Assertions.assertThat

import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized

import com.lesfurets.jenkins.unit.BaseRegressionTest
import com.lesfurets.jenkins.unit.MethodCall

@RunWith(Parameterized.class)
class TestJenkinsFile extends BaseRegressionTest {

    @Parameterized.Parameter
    public String branch
    @Parameterized.Parameter(1)
    public String expectedPhase

    @Parameterized.Parameters(name = "Test branch {0} phase {1}")
    static Collection<Object[]> data() {
        return [['develop', 'verify'],
                ['master', 'deploy'],
                ['feature_', 'verify']
        ].collect { it as Object[] }
    }

    @Override
    @Before
    void setUp() throws Exception {
        scriptRoots += 'src/test/jenkins'
        super.setUp()
        def scmBranch = branch
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
        helper.registerAllowedMethod("junit", [Map.class], null)
        helper.registerAllowedMethod("file", [Map.class], stringInterceptor)
        helper.registerAllowedMethod("archiveArtifacts", [String.class], null)
    }

    @Test
    void name() throws Exception {
        runScript("Jenkinsfile")
        super.testNonRegression(branch)
        assertThat(helper.callStack.stream()
                        .filter { c -> c.methodName == "sh" }
                        .map(MethodCall.&callArgsToString)
                        .findAll { s -> s.contains("./gradlew $expectedPhase") })
                        .hasSize(1)
    }
}
