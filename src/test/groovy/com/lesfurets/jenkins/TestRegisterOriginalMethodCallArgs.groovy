package com.lesfurets.jenkins

import org.junit.Before
import org.junit.Test

import com.lesfurets.jenkins.unit.BasePipelineTest

import static org.junit.Assert.*

class TestRegisterOriginalMethodCallArgs extends BasePipelineTest {

    @Override
    @Before
    void setUp() throws Exception {
        scriptRoots += "src/test/jenkins"
        super.setUp()
    }

    @Test
    void should_not_always_clone_args() {
        helper.cloneArgsOnMethodCallRegistration = false

        runScript("job/immutableMapArgs.jenkins")

        def arg = helper.callStack.find { call ->
            call.methodName == "writeFile"
        }.args.first()

        //Ensure that the arg is the original uncloned binding variable, and 
        //that we can inspect it in detail using all the normal Map interfaces.
        assertTrue(arg.is(binding.pretendArgsFromFarUpstream))

        assertEquals(arg.getClass().simpleName, "UnmodifiableMap")
        assertEquals(arg.size(), 2)

        assertEquals(arg.file, "foo.txt")
        assertEquals(arg.text, "All bar, all the time")
    }

    @Test
    void should_usually_clone_args() {
        //By default the helper clones args on registering calls.

        runScript("job/immutableMapArgs.jenkins")

        def arg = helper.callStack.find { call ->
            call.methodName == "writeFile"
        }.args.first()

        //Ensure that the arg is not the original binding variable, and not 
        //even the same type, because that variable was an uncloneable 
        //UnmodifiableMap. The cloning logic turns uncloneables into Strings.
        assertFalse(arg.is(binding.pretendArgsFromFarUpstream))

        assertEquals(arg.getClass().simpleName, "String")

        assertTrue(arg.contains("file:foo.txt") || arg.contains("file=foo.txt"))
        assertTrue(arg.contains("text:All bar, all the time") || arg.contains("text=All bar, all the time"))
    }
}
