package com.lesfurets.jenkins.unit.global.lib

import groovy.transform.CompileStatic

@CompileStatic
class LibraryRecord {

    LibraryConfiguration configuration
    String version
    List<String> rootPaths

    Map<String, Object> definedGlobalVars

    LibraryRecord(LibraryConfiguration configuration, String version, List<String> rootPaths) {
        this.configuration = configuration
        this.version = version
        this.rootPaths = rootPaths
    }

    String getIdentifier() {
        return "$configuration.name@$version"
    }

}
