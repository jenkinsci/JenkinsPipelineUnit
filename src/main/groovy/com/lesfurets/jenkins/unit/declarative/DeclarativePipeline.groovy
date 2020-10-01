package com.lesfurets.jenkins.unit.declarative

import static groovy.lang.Closure.*

class DeclarativePipeline extends GenericPipelineDeclaration {

    def properties = [:]
    List<Closure> options = []

    Closure triggers
    ParametersDeclaration parameters = null

    DeclarativePipeline() {
        properties.put('any', 'any')
        properties.put('none', 'none')
        properties.put('scm', 'scm')
    }

    def propertyMissing(String name) {
        if (properties.containsKey(name)) {
            return properties.get(name)
        } else {
            throw new MissingPropertyException(name)
        }
    }

    def options(@DelegatesTo(DeclarativePipeline) Closure closure) {
        options.add(closure)
    }

    def triggers(@DelegatesTo(DeclarativePipeline) Closure closure) {
        this.triggers = closure
    }

    def parameters(Object o) {
        this.parameters = new ParametersDeclaration().with { it.label = o; it }
    }

    def parameters(@DelegatesTo(strategy=DELEGATE_FIRST, value=ParametersDeclaration) Closure closure) {
        this.parameters = createComponent(ParametersDeclaration, closure)
    }

    def execute(Object delegate) {
        super.execute(delegate)
        this.options.forEach {
            executeWith(delegate, it)
        }
        this.agent?.execute(delegate)
        executeWith(delegate, this.triggers)
        this.stages.entrySet().forEach { e ->
            e.value.execute(delegate)
        }
        this.post?.execute(delegate)
    }

}
