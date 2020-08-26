package com.lesfurets.jenkins.unit.declarative.kubernetes

import groovy.transform.Memoized
import groovy.transform.ToString

import static com.lesfurets.jenkins.unit.declarative.GenericPipelineDeclaration.createComponent
import static com.lesfurets.jenkins.unit.declarative.ObjectUtils.printNonNullProperties
import static groovy.lang.Closure.DELEGATE_ONLY

@ToString(includePackage = false, includeNames = true, ignoreNulls = true)
class ContainerTemplate {

    String name;
    String image;
    boolean privileged;
    Long runAsUser;
    Long runAsGroup;
    boolean alwaysPullImage;
    String workingDir;
    String command;
    String args;
    boolean ttyEnabled;
    String resourceRequestCpu;
    String resourceRequestMemory;
    String resourceLimitCpu;
    String resourceLimitMemory;
    String shell;
    final List<TemplateEnvVar> envVars;
    List<PortMapping> ports;
    ContainerLivenessProbe livenessProbe;

    def name(final String name) {
        this.name = name
    }

    def image(final String image) {
        this.image = image
    }

    def privileged(final boolean privileged) {
        this.privileged = privileged
    }

    def runAsUser(final Long runAsUser) {
        this.runAsUser = runAsUser
    }

    def runAsGroup(final Long runAsGroup) {
        this.runAsGroup = runAsGroup
    }

    def alwaysPullImage(final boolean alwaysPullImage) {
        this.alwaysPullImage = alwaysPullImage
    }

    def workingDir(final String workingDir) {
        this.workingDir = workingDir
    }

    def command(final String command) {
        this.command = command
    }

    def args(final String args) {
        this.args = args
    }

    def ttyEnabled(final boolean ttyEnabled) {
        this.ttyEnabled = ttyEnabled
    }

    def resourceRequestCpu(final String resourceRequestCpu) {
        this.resourceRequestCpu = resourceRequestCpu
    }

    def resourceRequestMemory(final String resourceRequestMemory) {
        this.resourceRequestMemory = resourceRequestMemory
    }

    def resourceLimitCpu(final String resourceLimitCpu) {
        this.resourceLimitCpu = resourceLimitCpu
    }

    def resourceLimitMemory(final String resourceLimitMemory) {
        this.resourceLimitMemory = resourceLimitMemory
    }

    def shell(final String shell) {
        this.shell = shell
    }

    def ports(@DelegatesTo(strategy = DELEGATE_ONLY, value = PortMapping) List<Closure> closures) {
        this.ports = closures.each { ct ->
            return createComponent(PortMapping, ct)
        } as List<PortMapping>
    }

    def livenessProbe(@DelegatesTo(strategy = DELEGATE_ONLY, value = ContainerLivenessProbe) Closure closure) {
        this.livenessProbe = createComponent(ContainerLivenessProbe, ct)
    }

    @Memoized
    String toString() {
        return printNonNullProperties(this)
    }
}
