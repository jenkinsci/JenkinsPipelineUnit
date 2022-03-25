package com.lesfurets.jenkins.unit.declarative

import org.junit.Before
import org.junit.Test

class TestMockLocalFunction extends DeclarativePipelineTest {

    private static String SCRIPT_NO_PARAMS = "Mock_existing_function_Jenkinsfile"
    private static String SCRIPT_PARAMS = SCRIPT_NO_PARAMS + "_params"

    @Before
    @Override
    void setUp() throws Exception {
        scriptRoots += 'src/test/jenkins/jenkinsfiles'
        helper.registerAllowedMethod("githubNotify", [Map])
        super.setUp()
    }

    @Test(expected=MissingMethodException.class)
    void no_params_should_execute_with_errors() {
        runScript(SCRIPT_NO_PARAMS)
    }

    @Test
    void no_params_should_execute_without_errors() {
        helper.registerAllowedMethod("runFunc")
        runScript(SCRIPT_NO_PARAMS)
    }

    @Test(expected=MissingMethodException.class)
    void params_should_execute_with_errors() {
        runScript(SCRIPT_PARAMS)
    }

    @Test
    void params_should_execute_without_errors() {
        helper.registerAllowedMethod("runFuncWithParam", [String])
        runScript(SCRIPT_PARAMS)
    }
}
