package com.lesfurets.jenkins.unit.declarative.kubernetes

import com.lesfurets.jenkins.unit.declarative.GenericPipelineDeclaration
import groovy.transform.Memoized
import groovy.transform.ToString

import static com.lesfurets.jenkins.unit.declarative.ObjectUtils.printNonNullProperties

@ToString(includePackage = false, includeNames = true, ignoreNulls = true)
class PortMappingDeclaration extends GenericPipelineDeclaration {
    String name
    int containerPort
    int hostPort

    def name(final String name) {
        this.name = name
    }

    def containerPort(final int containerPort) {
        this.containerPort = containerPort
    }

    def hostPort(final int hostPort) {
        this.hostPort = hostPort
    }

    @Memoized
    String toString() {
        return printNonNullProperties(this)
    }
}
