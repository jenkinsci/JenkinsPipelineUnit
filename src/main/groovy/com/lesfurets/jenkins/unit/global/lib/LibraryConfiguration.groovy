package com.lesfurets.jenkins.unit.global.lib

import groovy.transform.ToString
import groovy.transform.builder.Builder

/**
 * Mock for org.jenkinsci.plugins.workflow.libs.LibraryConfiguration
 */
@Builder(builderMethodName = "library")
@ToString
class LibraryConfiguration {

    String name
    String defaultVersion = 'master'
    SourceRetriever retriever
    boolean implicit = false
    boolean allowOverride = true
    String targetPath

}