package com.lesfurets.jenkins.unit.declarative

import static groovy.lang.Closure.*

class DeclarativePipeline extends GenericPipelineDeclaration {

    def properties = [:]
    List<Closure> options = []
    def binding = null

    Closure triggers
    ParametersDeclaration params = null

    DeclarativePipeline() {
        properties.put('any', 'any')
        properties.put('none', 'none')
        properties.put('scm', 'scm')
    }

    def getParams() {
        return binding?.params
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

    def parameters(Object o) {
        this.params = new ParametersDeclaration().with { it.label = o; it }
    }

    def parameters(@DelegatesTo(strategy=Closure.DELEGATE_ONLY, value=ParametersDeclaration) Closure closure) {
        this.params = createComponent(ParametersDeclaration, closure)
    }

    def execute(Object delegate) {
        super.execute(delegate)
        this.options.forEach {
            executeOn(delegate, it)
        }
        this.agent?.execute(delegate)
        executeOn(delegate, this.triggers)
        this.stages.entrySet().forEach { e ->
            e.value.execute(delegate)
        }
        this.post?.execute(delegate)
    }

}
