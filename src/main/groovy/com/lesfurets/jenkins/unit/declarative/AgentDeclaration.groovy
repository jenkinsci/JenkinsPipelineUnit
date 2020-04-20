package com.lesfurets.jenkins.unit.declarative

import static com.lesfurets.jenkins.unit.declarative.DeclarativePipeline.createComponent
import static com.lesfurets.jenkins.unit.declarative.DeclarativePipeline.executeWith
import static com.lesfurets.jenkins.unit.declarative.ObjectUtils.printNonNullProperties
import static groovy.lang.Closure.*

import groovy.transform.Memoized
import groovy.transform.ToString

class AgentDeclaration {

    String label
    DockerDeclaration docker
    Boolean dockerfile = null
    String dockerfileDir
    Boolean reuseNode = null
    String customWorkspace

    def label(String label) {
        this.label = label
    }

    def node(@DelegatesTo(AgentDeclaration) Closure closure) {
        closure.call()
    }

    def customWorkspace(String workspace) {
        this.customWorkspace = workspace
    }

    def reuseNode(boolean reuse) {
        this.reuseNode = reuse
    }

    def docker(String image) {
        this.docker = new DockerDeclaration().with { it.image = image; it }
    }

    def docker(@DelegatesTo(strategy = DELEGATE_ONLY, value=DockerDeclaration) Closure closure) {
        this.docker = createComponent(DockerDeclaration, closure)
    }

    def dockerfile(boolean dockerfile) {
        this.dockerfile = dockerfile
    }

    def dockerfile(@DelegatesTo(AgentDeclaration) Closure closure) {
        closure.call()
    }

    def dir(String dir) {
        this.dockerfileDir = dir
    }

    def execute(Object delegate) {
        def agentDesc = null
        if (!label && !docker && dockerfile == null) {
            throw new IllegalStateException("No agent description found")
        }
        agentDesc = printNonNullProperties(this)
        executeWith(delegate, { echo "Executing on agent $agentDesc" })
    }

    @ToString(includePackage = false, includeNames = true, ignoreNulls = true)
    class DockerDeclaration {

        String image
        String label
        String args
        Boolean alwaysPull

        def image(String image) {
            this.image = image
        }

        def label(String label) {
            this.label = label
        }

        def args(String args) {
            this.args = args
        }
        def alwaysPull(Boolean alwaysPull) {
            this.alwaysPull = alwaysPull
        }

        @Memoized
        String toString() {
            return printNonNullProperties(this)
        }
    }

}
