package com.lesfurets.jenkins

import com.lesfurets.jenkins.unit.BasePipelineTest
import org.junit.Before
import org.junit.Test

import static org.junit.Assert.assertTrue

class TestCustomMethodJob extends BasePipelineTest {

    @Override
    @Before
    void setUp() throws Exception {
        scriptRoots += 'src/test/jenkins'
        super.setUp()
    }

    @Test
    void should_run_script_with_custom_method() {
        // when:
        runScript("job/customMethod.jenkins")

        // then:
        assertJobStatusSuccess()
        assertTrue(helper.callStack.find { call ->
            call.methodName == 'echo'
        }.argsToString() == 'executing custom method closure')
    }

    @Test
    void should_run_script_with_custom_method_mock() {
        // given:
        Closure customMethodMock = { echo 'executing mock closure' }
        helper.registerAllowedMethod('customMethod', [], customMethodMock)

        // when:
        runScript("job/customMethod.jenkins")

        // then:
        assertJobStatusSuccess()
        assertTrue(helper.callStack.find { call ->
            call.methodName == 'echo'
        }.argsToString() == 'executing mock closure')
    }

    @Test
    void should_run_script_with_custom_method_with_arguments() {
        // when:
        runScript("job/customMethodWithArguments.jenkins")

        // then:
        assertJobStatusSuccess()
        assertTrue(helper.callStack.find { call ->
            call.methodName == 'echo'
        }.argsToString() == 'executing custom method with arguments closure (arguments: \'stringArg\', \'42\', \'[collectionArg, 42]\')')
    }

    @Test
    void should_run_script_with_custom_method_with_arguments_mock() {
        // given:
        Closure customMethodWithArgumentsMock = { String stringArg, int intArg, Collection collectionArg ->
            echo "executing mock closure with arguments (arguments: '${stringArg}', '${intArg}', '${collectionArg}')"
        }
        helper.registerAllowedMethod('customMethodWithArguments', [String, int, Collection], customMethodWithArgumentsMock)

        // when:
        runScript("job/customMethodWithArguments.jenkins")

        // then:
        assertJobStatusSuccess()
        assertTrue(helper.callStack.find { call ->
            call.methodName == 'echo'
        }.argsToString() == 'executing mock closure with arguments (arguments: \'stringArg\', \'42\', \'[collectionArg, 42]\')')
    }
}
