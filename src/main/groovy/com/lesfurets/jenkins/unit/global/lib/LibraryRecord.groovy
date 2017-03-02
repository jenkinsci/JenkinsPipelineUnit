package com.lesfurets.jenkins.unit.global.lib

import groovy.transform.Canonical

@Canonical
class LibraryRecord {

    LibraryConfiguration configuration
    String version
    Map<String, Object> definedGlobalVars

    String getIdentifier() {
        return "$configuration.name@$version"
    }

}
