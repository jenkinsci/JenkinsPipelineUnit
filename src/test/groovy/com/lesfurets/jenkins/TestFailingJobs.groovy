/*
 * Copyright (C) by Courtanet, All Rights Reserved.
 */
package com.lesfurets.jenkins

import com.lesfurets.jenkins.unit.cps.BasePipelineTestCPS
import org.codehaus.groovy.runtime.typehandling.GroovyCastException
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test

import static org.junit.jupiter.api.Assertions.assertThrows

class TestFailingJobs extends BasePipelineTestCPS {

    @Override
    @BeforeEach
    void setUp() throws Exception {
        scriptRoots += 'src/test/jenkins'
        super.setUp()
    }

    @Test
    void should_fail_nonCpsCallingCps() throws Exception {
        assertThrows(GroovyCastException, { ->
            def  script = runScript("job/shouldFail/nonCpsCallingCps.jenkins")
            printCallStack()
        })
    }

    /**
     * java.lang.UnsupportedOperationException: Calling public static java.util.List
     * org.codehaus.groovy.runtime.DefaultGroovyMethods.each(java.util.List,groovy.lang.Closure)
     * on a CPS-transformed closure is not yet supported (JENKINS-26481);
     * encapsulate in a @NonCPS method, or use Java-style loops
     */
    @Test
    @Disabled
    void should_fail_forEach() throws Exception {
        assertThrows(UnsupportedOperationException, { ->
            def  script = runScript("job/shouldFail/forEach.jenkins")
            printCallStack()
        })
    }
}
