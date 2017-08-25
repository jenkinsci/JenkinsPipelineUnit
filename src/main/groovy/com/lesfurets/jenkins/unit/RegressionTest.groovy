package com.lesfurets.jenkins.unit

trait RegressionTest {

    String callStackPath = "src/test/resources/callstacks/"

    /**
     * Checks the current callstack is the same as the reference callstack.
     * The reference callstack can be updated into a txt file in the callStackPath
     *
     * Pattern: <RegressionTest.callStackPath>/<ClassTestSimpleName><_subname>.txt
     * @param subname optional subname, used in the reference callstack filename
     */
    void testNonRegression(String subname = '') {
        String targetFileName = "${callStackPath}${this.class.simpleName}"
        if (subname) {
            targetFileName += "_${subname}"
        }
        RegressionTestHelper.testNonRegression(helper, targetFileName)
    }

}
