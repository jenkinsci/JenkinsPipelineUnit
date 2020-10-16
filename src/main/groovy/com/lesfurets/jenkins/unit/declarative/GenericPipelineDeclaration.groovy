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
        def rehydrate = closure.rehydrate(closure, componentInstance, componentInstance)
        if (binding && componentInstance.hasProperty('binding') && componentInstance.binding != binding) {
            componentInstance.binding = binding
        }
        rehydrate.call()
        return componentInstance
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

    def stage(String stageName,
              @DelegatesTo(strategy = DELEGATE_FIRST, value = StageDeclaration) Closure closure) {
        this.stages.put(stageName, createComponent(StageDeclaration, closure).with { it.name = stageName; it })
    }

    def execute(Object delegate) {
        Map envValuestoRestore = [:]

        // set environment
        if (this.environment) {
            envValuestoRestore = initEnvironment(this.environment, delegate)
        }
        resetEnvironment(envValuestoRestore, delegate)
    }

    public static Map initEnvironment(Closure environment, Object delegate) {
        Map envValuestoRestore = [:]
        Binding subBinding = new Binding()
        subBinding.metaClass.invokeMissingProperty = { propertyName ->
            delegate.getProperty(propertyName)
        }
        subBinding.metaClass.setProperty = { String propertyName, Object newValue ->
            if (delegate.hasProperty(propertyName)) {
                envValuestoRestore.put(propertyName, delegate.getProperty(propertyName))
            }
            (delegate.env as Map).put(propertyName, newValue)
        }
        def envClosure = environment.rehydrate(subBinding, delegate, this)
        envClosure.resolveStrategy = DELEGATE_FIRST
        envClosure.call()
        return envValuestoRestore
    }

    public static resetEnvironment(LinkedHashMap envValuestoRestore, delegate) {
        envValuestoRestore.entrySet().forEach { entry ->
            def envMap = delegate.env as Map
            envMap.put(entry.getKey(), entry.getValue())
        }
    }
}
