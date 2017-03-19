package com.lesfurets.jenkins.unit.declarative

import static com.lesfurets.jenkins.unit.declarative.DeclarativePipeline.createComponent

abstract class GenericPipelineDeclaration {

    AgentDeclaration agent
    Closure environment
    Closure tools
    PostDeclaration post

    def agent(Object o) {
        this.agent = new AgentDeclaration().with {
            it.label = o
            return it
        }
    }

    def agent(Closure closure) {
        this.agent = createComponent(AgentDeclaration, closure)
    }

    def environment(Closure closure) {
        this.environment = closure
    }

    def tools(Closure closure) {
        this.tools = closure
    }

    def post(Closure closure) {
        this.post = createComponent(PostDeclaration, closure)
    }

    def execute(Object delegate) {
        // set environment
        if (environment) {
            def env = delegate.binding.env
            def cl = this.environment.rehydrate(env, delegate, this)
            cl.resolveStrategy = Closure.DELEGATE_FIRST
            cl.call()
        }
    }

}
