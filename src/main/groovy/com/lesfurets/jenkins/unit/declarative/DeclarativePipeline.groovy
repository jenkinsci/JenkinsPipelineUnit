package com.lesfurets.jenkins.unit.declarative

import static groovy.lang.Closure.*

class DeclarativePipeline extends GenericPipelineDeclaration {

    def properties = [:]
    List<Closure> options = []

    Closure triggers
    Closure parameters

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

    def triggers(@DelegatesTo(DeclarativePipeline) Closure closure) {
        this.triggers = closure
    }

    def parameters(@DelegatesTo(DeclarativePipeline) Closure closure) {
        this.parameters = closure
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
