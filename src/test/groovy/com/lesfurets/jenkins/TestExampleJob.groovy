package com.lesfurets.jenkins

import com.lesfurets.jenkins.unit.BasePipelineTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

import static com.lesfurets.jenkins.unit.MethodCall.callArgsToString
import static org.junit.jupiter.api.Assertions.assertTrue

class TestExampleJob extends BasePipelineTest {

    @Override
    @BeforeEach
    void setUp() throws Exception {
        scriptRoots += 'src/test/jenkins'
        super.setUp()
        def scmBranch = "feature_test"
        helper.registerAllowedMethod("sh", [Map.class], {c -> 'bcc19744'})
        binding.setVariable('scm', [
                        $class                           : 'GitSCM',
                        branches                         : [[name: scmBranch]]
        ])
    }

    @Test
    void should_execute_without_errors() throws Exception {
        def script = runScript("job/exampleJob.jenkins")
        script.execute()
        printCallStack()
        assertJobStatusSuccess()
    }

    @Test
    void should_print_property_value() {
        def script = runScript('job/exampleJob.jenkins')
        script.execute()

        def value = 'value'
        assertTrue(helper.callStack.findAll { call ->
            call.methodName == 'println'
        }.any { call ->
            callArgsToString(call).contains(value)
        })
    }
}
