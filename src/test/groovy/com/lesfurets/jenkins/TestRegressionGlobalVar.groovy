package com.lesfurets.jenkins

import com.lesfurets.jenkins.unit.cps.BaseRegressionTestCPS
import org.junit.Before
import org.junit.Test

class TestRegressionGlobalVar extends BaseRegressionTestCPS {

    @Override
    @Before
    void setUp() throws Exception {
        scriptRoots += 'src/test/jenkins'
        super.setUp()
        helper.registerAllowedMethod("doWithProperties", [TreeMap.class], null)
    }

    @Test
    void testGlobalVarRegression() throws Exception {
        runScript("job/globalVar.jenkins")
        super.testNonRegression("globalVar")
    }

}
