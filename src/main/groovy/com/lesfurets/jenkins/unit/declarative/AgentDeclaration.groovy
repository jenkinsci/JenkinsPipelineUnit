package com.lesfurets.jenkins.unit.declarative

import com.lesfurets.jenkins.unit.declarative.agent.DockerAgentDeclaration
import com.lesfurets.jenkins.unit.declarative.agent.KubernetesAgentDeclaration

import static com.lesfurets.jenkins.unit.declarative.DeclarativePipeline.createComponent
import static com.lesfurets.jenkins.unit.declarative.DeclarativePipeline.executeWith
import static com.lesfurets.jenkins.unit.declarative.ObjectUtils.printNonNullProperties
import static groovy.lang.Closure.DELEGATE_ONLY

class AgentDeclaration {

    String label
    DockerAgentDeclaration docker
    KubernetesAgentDeclaration kubernetes
    Boolean dockerfile = null
    String dockerfileDir
    Boolean reuseNode = null
    String customWorkspace
    def binding = null

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
        this.docker = new DockerAgentDeclaration().with { it.image = image; it }
    }

    def docker(@DelegatesTo(strategy = DELEGATE_ONLY, value = DockerAgentDeclaration) Closure closure) {
        this.docker = createComponent(DockerAgentDeclaration, closure)
    }

    def kubernetes(@DelegatesTo(strategy = DELEGATE_ONLY, value = KubernetesAgentDeclaration) Closure closure) {
        this.kubernetes = createComponent(KubernetesAgentDeclaration, closure)
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

    def getCurrentBuild() {
        return binding?.currentBuild
    }

    def getEnv() {
        return binding?.env
    }

    def getParams() {
        return binding?.params
    }

    def execute(Object delegate) {
        def agentDesc = null
        if (!label && !docker && dockerfile == null && kubernetes == null) {
            throw new IllegalStateException("No agent description found")
        }
        agentDesc = printNonNullProperties(this)
        executeWith(delegate, { echo "Executing on agent $agentDesc" })
    }
}
