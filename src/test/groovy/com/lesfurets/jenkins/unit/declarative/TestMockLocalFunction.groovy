package com.lesfurets.jenkins.unit.declarative

import com.lesfurets.jenkins.unit.InterceptingGCL
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

import static org.assertj.core.api.Assertions.assertThat
import static org.junit.jupiter.api.Assertions.assertThrows

class TestMockLocalFunction extends DeclarativePipelineTest {

    private static String SCRIPT_NO_PARAMS = "Mock_existing_function_Jenkinsfile"
    private static String SCRIPT_PARAMS = SCRIPT_NO_PARAMS + "_params"

    @BeforeEach
    @Override
    void setUp() throws Exception {
        scriptRoots += 'src/test/jenkins/jenkinsfiles'
        helper.registerAllowedMethod("githubNotify", [Map])
        super.setUp()
    }

    @Test
    void no_params_should_execute_with_errors() {
        assertThrows(MissingMethodException.class, { ->
            runScript(SCRIPT_NO_PARAMS)
        })
    }

    @Test
    void no_params_should_execute_without_errors() {
        helper.registerAllowedMethod("runFunc")
        runScript(SCRIPT_NO_PARAMS)
    }

    @Test
    void params_should_execute_with_errors() {
        assertThrows(MissingMethodException.class, { ->
            runScript(SCRIPT_PARAMS)
        })
    }

    @Test
    void params_should_execute_without_errors() {
        helper.registerAllowedMethod("runFuncWithParam", [String])
        runScript(SCRIPT_PARAMS)
    }

    @Test
    void default_closure_empty() {
        Class[] args = []
        default_closure_parameter_check(args)
    }

    @Test
    void default_closure_different_args() {
        Class[] args = [Arrays.class, Test.class, InterceptingGCL.class, String.class, Integer.class]
        default_closure_parameter_check(args)
    }

    @Test
    void default_closure_max_args() {
        Class[] args = (1..254).collect{ String.class }
        default_closure_parameter_check(args)
    }

    @Test
    void default_closure_exceeded_args() {
        assertThrows(IllegalArgumentException.class, { ->
            Class[] args = (1..500).collect {
                String.class
            }
            default_closure_parameter_check(args)
        })
    }

    private static void default_closure_parameter_check(Class[] args) {
        Closure closure = InterceptingGCL.defaultClosure(args)
        assertThat(closure.getParameterTypes()).isEqualTo(args)
    }
}
