package com.lesfurets.jenkins.unit.declarative

import com.lesfurets.jenkins.unit.declarative.agent.DockerAgentDeclaration
import com.lesfurets.jenkins.unit.declarative.agent.DockerFileAgentDeclaration
import com.lesfurets.jenkins.unit.declarative.agent.KubernetesAgentDeclaration
import groovy.transform.ToString

import static groovy.lang.Closure.DELEGATE_FIRST

@ToString(includePackage = false, includeNames = true, ignoreNulls = true)
class AgentDeclaration extends GenericPipelineDeclaration {

    String label
    DockerAgentDeclaration docker
    KubernetesAgentDeclaration kubernetes
    DockerFileAgentDeclaration dockerfileAgent
    String customWorkspace
    def binding = null
    String registryCredentialsId = null
    String registryUrl = null

    def label(String label) {
        this.label = label
    }

    def node(@DelegatesTo(AgentDeclaration) Closure closure) {
        closure.call()
    }

    def customWorkspace(String workspace) {
        this.customWorkspace = workspace
    }

    def docker(String image) {
        this.docker = new DockerAgentDeclaration().with { it.image = image; it }
    }

    def docker(@DelegatesTo(strategy = DELEGATE_FIRST, value = DockerAgentDeclaration) Closure closure) {
        this.docker = createComponent(DockerAgentDeclaration, closure)
    }

    def kubernetes(boolean _) {
        kubernetes([:])
    }

    def kubernetes(Object kubernetesAgent) {
        this.@kubernetes = kubernetesAgent as KubernetesAgentDeclaration
    }

    def kubernetes(@DelegatesTo(strategy = DELEGATE_FIRST, value = KubernetesAgentDeclaration) Closure closure) {
        this.@kubernetes = createComponent(KubernetesAgentDeclaration, closure)
    }

    def dockerfile(boolean _) {
        dockerfile([:])
    }

    def dockerfile(Object dockerfile) {
        this.@dockerfileAgent = dockerfile as DockerFileAgentDeclaration
    }

    def dockerfile(@DelegatesTo(strategy = DELEGATE_FIRST, value = DockerFileAgentDeclaration) Closure closure) {
        this.@dockerfileAgent = createComponent(DockerFileAgentDeclaration, closure)
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

    def registryCredentialsId(String registryCredentialsId) {
        this.registryCredentialsId = registryCredentialsId
    }

    def registryUrl(String registryUrl) {
        this.registryUrl = registryUrl
    }

    def execute(Object delegate) {
        def agentDesc = null

        if (label != null) {
            agentDesc = '[label:' + label.toString() + ']'
        }
        else if (docker) {
            agentDesc = '[docker:' + docker.toString() + ']'
        }
        else if (dockerfileAgent) {
            agentDesc = '[dockerfile:' + dockerfileAgent.toString() + ']'
        }
        else if (kubernetes) {
            agentDesc = '[kubernetes:' + kubernetes.toString() + ']'
        }
        else {
            throw new IllegalStateException("No agent description found")
        }
        executeWith(delegate, { echo "Executing on agent $agentDesc" })
    }
}
