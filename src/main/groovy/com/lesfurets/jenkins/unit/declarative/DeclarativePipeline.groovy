package com.lesfurets.jenkins.unit.declarative

import static groovy.lang.Closure.*

class DeclarativePipeline extends GenericPipelineDeclaration {

    def properties = [:]
    List<Closure> options = []

    Closure triggers
    ParametersDeclaration params = null

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

    def parameters(@DelegatesTo(strategy=DELEGATE_FIRST, value=ParametersDeclaration) Closure closure) {
        this.params = createComponent(ParametersDeclaration, this.binding, closure)
    }

    def execute(Object delegate) {
        Throwable failure = null
        try {
            super.execute(delegate)
            this.options.each {
                executeOn(delegate, it)
            }
            this.agent?.execute(delegate)
            executeOn(delegate, this.triggers)
            executeStages(delegate)
        } catch (Throwable t) {
            failure = recordFailure(delegate, null, t)
        }
        // post runs whether or not the pipeline failed; the failure is then rethrown so the
        // test still sees it, just like Jenkins still fails the build.
        failure = executePost(delegate, failure)
        if (failure != null) {
            throw failure
        }
    }

}
