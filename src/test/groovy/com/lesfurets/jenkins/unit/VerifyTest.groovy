package com.lesfurets.jenkins.unit

import org.junit.Before
import org.junit.Test

class VerifyTest extends BasePipelineTest {

    static final String PARAMETER1 = "someString"
    static final String PARAMETER2 = "anotherString"
    static final String SOME_STRING_METHOD_NAME = "someStringMethod"
    static final String VOID_METHOD_NAME = "voidMethod"

    Script script

    @Before
    void beforeTest() {
        setUp()
        script = loadScript('src/test/jenkins/job/verify.jenkins')
    }

    @Test
    void verify_some_string_method() {
        script.someStringMethod(PARAMETER1)
        script.someStringMethod(PARAMETER1, PARAMETER2)
        helper.verify(SOME_STRING_METHOD_NAME, 2)
    }

    @Test
    void verify_some_string_method_parameter1() {
        script.someStringMethod(PARAMETER1)
        helper.verify(SOME_STRING_METHOD_NAME, 1) { methodCall ->
            return methodCall.args[0].toString() == PARAMETER1
        }
    }

    @Test
    void verify_some_string_method_parameter2() {
        script.someStringMethod(PARAMETER1, PARAMETER2)
        helper.verify(SOME_STRING_METHOD_NAME, 1) { methodCall ->
            Object[] arguments = methodCall.args
            return arguments.size() == 2 && arguments[0].toString() == PARAMETER1 && arguments[1].toString() == PARAMETER2
        }
    }

    @Test(expected = VerificationException.class)
    void verify_some_string_method_another_params() {
        script.someStringMethod(PARAMETER1, "another")
        helper.verify(SOME_STRING_METHOD_NAME, 1) { MethodCall methodCall ->
            Object[] arguments = methodCall.args
            return arguments.size() == 2 && arguments[0].toString() == PARAMETER1 && arguments[1].toString() == PARAMETER2
        }
    }

    @Test
    void verify_void_method() {
        script.voidMethod()
        helper.verify(VOID_METHOD_NAME)
    }

    @Test(expected = VerificationException.class)
    void verify_void_method_expect_param() {
        script.voidMethod()
        helper.verify(VOID_METHOD_NAME, 1) { methodCall -> methodCall.args.size() > 0 }
    }

    @Test(expected = VerificationException.class)
    void verify_void_method_less_times() {
        script.voidMethod()
        script.voidMethod()
        helper.verify(VOID_METHOD_NAME)
    }
}
