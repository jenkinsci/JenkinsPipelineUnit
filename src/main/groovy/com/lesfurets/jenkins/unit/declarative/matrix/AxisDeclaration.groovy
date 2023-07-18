package com.lesfurets.jenkins.unit.declarative.matrix


import com.lesfurets.jenkins.unit.declarative.GenericPipelineDeclaration
import groovy.transform.ToString

@ToString(includePackage = false, includeNames = true, ignoreNulls = true)
class AxisDeclaration extends GenericPipelineDeclaration {

    String name
    List<String> values = []

    def name(String name) {
        this.name = name
    }

    def values(String... values) {
        this.values = Arrays.asList(values)
    }
}

