package com.lesfurets.jenkins.unit.declarative

import static groovy.lang.Closure.DELEGATE_FIRST

abstract class GenericPipelineDeclaration {

    AgentDeclaration agent
    Closure environment
    Closure tools
    PostDeclaration post
    Map<String, StageDeclaration> stages = [:]
    static def binding = null

    static <T> T createComponent(Class<T> componentType, @DelegatesTo(strategy = DELEGATE_FIRST) Closure<T> closure) {
        // declare componentInstance as final to prevent any multithreaded issues, since it is used inside closure
        final def componentInstance = componentType.newInstance()
        def rehydrate = closure.rehydrate(closure, componentInstance, closure.thisObject)
        if (binding && componentInstance.hasProperty('binding') && componentInstance.binding != binding) {
            componentInstance.binding = binding
        }
        rehydrate.call()
        return componentInstance
    }

    static <T> T executeOn(@DelegatesTo.Target Object delegate,
                           @DelegatesTo(strategy = DELEGATE_FIRST) Closure<T> closure) {
        return executeWith(delegate, closure)
    }

    static <T> T executeWith(@DelegatesTo.Target Object delegate,
                             @DelegatesTo(strategy = DELEGATE_FIRST) Closure<T> closure) {
        if (closure) {
            def rehydratedClosure = closure.rehydrate(closure, delegate, closure.thisObject)
            rehydratedClosure.resolveStrategy = DELEGATE_FIRST
            return rehydratedClosure.call()
        }
        return null
    }

    def agent(Object o) {
        this.agent = new AgentDeclaration().with { it.label = o; it }
    }

    def agent(@DelegatesTo(strategy = DELEGATE_FIRST, value = AgentDeclaration) Closure closure) {
        this.agent = createComponent(AgentDeclaration, closure)
    }

    def environment(Closure closure) {
        this.environment = closure
    }

    def tools(Closure closure) {
        this.tools = closure
    }

    def post(@DelegatesTo(strategy = DELEGATE_FIRST, value = PostDeclaration) Closure closure) {
        this.post = createComponent(PostDeclaration, closure)
    }

    def stages(@DelegatesTo(DeclarativePipeline) Closure closure) {
        closure.call()
    }

    def stage(String name,
              @DelegatesTo(strategy = DELEGATE_FIRST, value = StageDeclaration) Closure closure) {
        this.stages.put(name, createComponent(StageDeclaration, closure).with { it.name = name; it })
    }

    def getProperty(String propertyName) {
        def metaProperty = this.metaClass.getMetaProperty(propertyName)
        if (metaProperty) {
            return metaProperty.getProperty(this)
        } else {
            if (binding?.hasProperty(propertyName) || binding?.hasVariable(propertyName)) {
                return binding.getProperty(propertyName)
            }
            if (binding?.hasVariable("params") && (binding?.getProperty("params") as Map).containsKey(propertyName)) {
                return (binding?.getProperty("params") as Map).get(propertyName)
            }
            if (binding?.hasVariable("env") && (binding?.getProperty("env") as Map).containsKey(propertyName)) {
                return (binding?.getProperty("env") as Map).get(propertyName)
            }
            def metaMethod = this.metaClass.getMetaMethod("propertyMissing", propertyName)
            if (metaMethod) {
                metaMethod.invoke(this, propertyName)
            } else {
                throw new MissingPropertyException(propertyName)
            }
        }
    }

    def execute(Object delegate) {
        // set environment
        if (this.environment) {
            def env = delegate.binding.env
            // let access env and currentBuild properties in environment closure
            env.env = env
            env.currentBuild = delegate.binding.currentBuild

            def rehydratedEnvClosure = this.environment.rehydrate(env, delegate, delegate)
            rehydratedEnvClosure.resolveStrategy = DELEGATE_FIRST
            rehydratedEnvClosure.call()
        }
    }

}
