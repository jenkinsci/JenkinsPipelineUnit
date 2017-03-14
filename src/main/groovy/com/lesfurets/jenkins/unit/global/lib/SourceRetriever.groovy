package com.lesfurets.jenkins.unit.global.lib

import groovy.transform.CompileStatic

@CompileStatic
interface SourceRetriever {

    public static final int CLONE_TIMEOUT = 10

    List<URL> retrieve(String repository, String branch, String targetPath) throws IllegalStateException

}
