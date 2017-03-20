package com.lesfurets.jenkins.unit.declarative

import static groovy.lang.Closure.*

class DeclarativePipeline extends GenericPipelineDeclaration {

    def properties = [:]
    List<Closure> options = []
    Map<String, StageDeclaration> stages = [:]

    Closure triggers
    Closure parameters

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

    static <T> T createComponent(Class<T> componentType,
                                 @DelegatesTo(strategy = DELEGATE_ONLY, value = T) Closure closure) {
        def componentInstance = componentType.newInstance()
        def rehydrate = closure.rehydrate(componentInstance, this, this)
        rehydrate.resolveStrategy = DELEGATE_ONLY
        rehydrate.call()
        return componentInstance
    }

    DeclarativePipeline() {
        properties.put('any', 'any')
        properties.put('none', 'none')
        properties.put('scm', 'scm')
    }

    def propertyMissing(String name) {
        if (properties.containsKey(name)) {
            return properties.get(name)
        } else {
            throw new IllegalStateException("Missing $name")
        }
    }

    def propertyMissing(String name, arg) {

    }

    def options(@DelegatesTo(DeclarativePipeline) Closure closure) {
        options.add(closure)
    }

    def stages(@DelegatesTo(DeclarativePipeline) Closure closure) {
        closure.call()
    }

    def triggers(@DelegatesTo(DeclarativePipeline) Closure closure) {
        this.triggers = closure
    }

    def parameters(@DelegatesTo(DeclarativePipeline) Closure closure) {
        this.parameters = closure
    }

    def stage(String name,
              @DelegatesTo(strategy = DELEGATE_ONLY, value = StageDeclaration) Closure closure) {
        this.stages.put(name, createComponent(StageDeclaration, closure).with { it.name = name; it })
    }

    def execute(Object delegate) {
        super.execute(delegate)
        this.options.forEach {
            executeOn(delegate, it)
        }
        this.agent?.execute(delegate)
        executeOn(delegate, this.parameters)
        executeOn(delegate, this.triggers)
        this.stages.entrySet().forEach { e ->
            e.value.execute(delegate)
        }
        this.post?.execute(delegate)
    }

}
