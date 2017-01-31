package com.lesfurets.jenkins.helpers

trait RegressionTest {

    String callStackPath = "src/test/resources/callstacks/"

    void testNonRegression(String subname = '', boolean writeReference) {
        String targetFileName = "${callStackPath}${this.class.simpleName}"
        if (subname) {
            targetFileName += "_${subname}"
        }
        RegressionTestHelper.testNonRegression(helper, targetFileName, writeReference)
    }

}