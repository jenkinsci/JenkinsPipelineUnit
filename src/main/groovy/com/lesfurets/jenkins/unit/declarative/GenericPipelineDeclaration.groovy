package com.lesfurets.jenkins.unit.declarative


import static groovy.lang.Closure.DELEGATE_FIRST
import static groovy.lang.Closure.DELEGATE_ONLY

abstract class GenericPipelineDeclaration {

    AgentDeclaration agent
    Closure environment
    Closure tools
    PostDeclaration post
    Map<String, StageDeclaration> stages = [:]
    static def binding = null

    static <T> T createComponent(Class<T> componentType,
                                 @DelegatesTo(strategy = DELEGATE_ONLY) Closure<T> closure) {
        def componentInstance = componentType.newInstance()
        def rehydrate = closure.rehydrate(componentInstance, this, this)
        rehydrate.resolveStrategy = DELEGATE_ONLY
        if (binding && componentInstance.hasProperty('binding') && componentInstance.binding != binding) {
            componentInstance.binding = binding
        }
        rehydrate.call()
        return componentInstance
    }

    static <T> T executeOn(@DelegatesTo.Target Object delegate,
                           @DelegatesTo(strategy = DELEGATE_ONLY) Closure<T> closure) {
        if (closure) {
            def cl = closure.rehydrate(delegate, delegate, delegate)
            cl.resolveStrategy = DELEGATE_ONLY
            return cl.call()
        }
        return null
    }

    static <T> T executeWith(@DelegatesTo.Target Object delegate,
                             @DelegatesTo(strategy = DELEGATE_FIRST) Closure<T> closure) {
        if (closure) {
            def cl = closure.rehydrate(delegate, delegate, delegate)
            cl.resolveStrategy = DELEGATE_FIRST
            return cl.call()
        }
        return null
    }

    def agent(Object o) {
        this.agent = new AgentDeclaration().with { it.label = o; it }
    }

    def agent(@DelegatesTo(strategy = Closure.DELEGATE_ONLY, value = AgentDeclaration) Closure closure) {
        this.agent = createComponent(AgentDeclaration, closure)
    }

    def environment(Closure closure) {
        this.environment = closure
    }

    def tools(Closure closure) {
        this.tools = closure
    }

    def post(@DelegatesTo(strategy = DELEGATE_ONLY, value = PostDeclaration) Closure closure) {
        this.post = createComponent(PostDeclaration, closure)
    }

    def stages(@DelegatesTo(DeclarativePipeline) Closure closure) {
        closure.call()
    }

    def stage(String name,
              @DelegatesTo(strategy = DELEGATE_ONLY, value = StageDeclaration) Closure closure) {
        this.stages.put(name, createComponent(StageDeclaration, closure).with { it.name = name; it })
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
        // set environment
        if (this.environment) {
            def env = delegate.binding.env
            // let access env and currentBuild properties in environment closure
            env.env = env
            env.currentBuild = delegate.binding.currentBuild

            def cl = this.environment.rehydrate(env, delegate, this)
            cl.resolveStrategy = Closure.DELEGATE_FIRST
            cl.call()
        }
    }

}
