package com.lesfurets.jenkins.unit.global.lib

abstract class SourceRetriever {

    public static final int CLONE_TIMEOUT = 10

    String sourceURL

    abstract List<URL> retrieve(String repository, String branch, String targetPath) throws IllegalStateException

}
