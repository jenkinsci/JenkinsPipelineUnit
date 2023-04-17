package com.lesfurets.jenkins.unit.declarative.agent


import com.lesfurets.jenkins.unit.declarative.GenericPipelineDeclaration
import groovy.transform.ToString

@ToString(includePackage = false, includeNames = true, ignoreNulls = true)
class DockerfileAgentDeclaration extends GenericPipelineDeclaration {
    String additionalBuildArgs
    String args
    String customWorkspace
    String dockerfileDir
    String filename
    String label
    String registryCredentialsId
    String registryUrl
    Boolean reuseNode

    def additionalBuildArgs(String additionalBuildArgs) {
        this.additionalBuildArgs = additionalBuildArgs
    }

    def args(String args) {
        this.args = args
    }

    def customWorkspace(final String customWorkspace) {
        this.customWorkspace = customWorkspace
    }

    def dir(String dir) {
        this.dockerfileDir = dir
    }

    def filename(String filename) {
        this.filename = filename
    }

    def label(final String label) {
        this.label = label
    }

    def registryCredentialsId(final String registryCredentialsId) {
        this.registryCredentialsId = registryCredentialsId
    }

    def registryUrl(final String registryUrl) {
        this.registryUrl = registryUrl
    }

    def reuseNode(boolean reuse) {
        this.reuseNode = reuse
    }

}
