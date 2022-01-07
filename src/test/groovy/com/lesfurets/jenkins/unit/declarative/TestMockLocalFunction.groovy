package com.lesfurets.jenkins.unit.declarative

import org.junit.Before
import org.junit.Test

class TestMockLocalFunction extends DeclarativePipelineTest {

    @Before
    @Override
    void setUp() throws Exception {
        scriptRoots += 'src/test/jenkins/jenkinsfiles'
        helper.registerAllowedMethod("githubNotify", [Map])
        super.setUp()
    }

    @Test(expected=MissingMethodException.class)
    void should_execute_with_errors() {
        runScript("Mock_existing_function_Jenkinsfile")
    }

    @Test
    void should_execute_without_errors() {
        helper.registerAllowedMethod("runFunc")
        runScript("Mock_existing_function_Jenkinsfile")
    }
}
