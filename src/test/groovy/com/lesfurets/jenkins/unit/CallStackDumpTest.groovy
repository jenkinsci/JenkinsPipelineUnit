package com.lesfurets.jenkins.unit

import org.junit.jupiter.api.Test

class CallStackDumpTest extends BasePipelineTest {

    @Test
    void test_callStackDump_returns_new_value() {
        setUp()
        def script = loadScript('src/test/jenkins/job/callStackDump.jenkins')

        // Call method1 and expect callstack to match
        script.someMethod1()
        assertCallStackContains('callStackDump.someMethod1()')

        // Reset using setUp call
        setUp()
        // Call method2 and expect callstack to match
        script.someMethod2()
        assertCallStackContains('callStackDump.someMethod2()')
        assertCallStack().doesNotContain('callStackDump.someMethod1()')

        // Reset using clearCallStack call
        clearCallStack()
        // Call method1 and expect callstack to match
        script.someMethod1()
        assertCallStackContains('callStackDump.someMethod1()')
    }

    @Test
    void test_callStackDump_returns_cached_value() {
        setUp()
        def script = loadScript('src/test/jenkins/job/callStackDump.jenkins')

        // Call method1 and expect callstack to match
        script.someMethod1()
        assertCallStackContains('callStackDump.someMethod1()')
        assertCallStack().doesNotContain('callStackDump.someMethod2()')

        // Do not clear call stack continue your test

        // Call method2 and expect callstack to match
        script.someMethod2()
        assertCallStackContains('callStackDump.someMethod1()')
        assertCallStackContains('callStackDump.someMethod2()')

    }
}
