package com.lesfurets.jenkins.unit.declarative

import static com.lesfurets.jenkins.unit.declarative.ObjectUtils.printNonNullProperties

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

    def node(Closure closure) {
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

    def docker(Closure closure) {
        def dockerDeclaration = new DockerDeclaration()
        def cl = closure.rehydrate(dockerDeclaration, this, this)
        cl.resolveStrategy = Closure.DELEGATE_ONLY
        cl.call()
        this.docker = dockerDeclaration

    }

    def dockerfile(boolean dockerfile) {
        this.dockerfile = dockerfile
    }

    def dockerfile(Closure closure) {
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
        Closure cl = { echo "Executing on agent $agentDesc" }
        cl.rehydrate(delegate, this, this).call()
    }

    @ToString(includePackage = false, includeNames = true, ignoreNulls = true)
    class DockerDeclaration {

        String image
        String label
        String args

        def image(String image) {
            this.image = image
        }

        def label(String label) {
            this.label = label
        }

        def args(String args) {
            this.args = args
        }

        @Memoized
        String toString() {
            return printNonNullProperties(this)
        }
    }

}
