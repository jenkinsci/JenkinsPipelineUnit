package com.lesfurets.jenkins.unit.declarative

class DeclarativePipeline extends GenericPipelineDeclaration {

    def properties = [:]
    List<Closure> options = []
    Map<String, StageDeclaration> stages = [:]

    Closure triggers
    Closure parameters

    static executeOn(Closure closure, Object delegate) {
        if (closure) {
            def cl = closure.rehydrate(delegate, this, this)
            cl.resolveStrategy = Closure.DELEGATE_ONLY
            cl.call()
        }
    }

    static <T> T createComponent(Class<T> componentType, Closure closure) {
        def componentInstance = componentType.newInstance()
        def rehydrate = closure.rehydrate(componentInstance, this, this)
        rehydrate.resolveStrategy = Closure.DELEGATE_ONLY
        rehydrate.call()
        return  componentInstance
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

    def options(Closure closure) {
        options.add(closure)
    }

    def stages(Closure closure) {
        closure.call()
    }

    def triggers(Closure closure) {
        this.triggers = closure
    }

    def parameters(Closure closure) {
        this.parameters = closure
    }

    def stage(String name, Closure closure) {
        this.stages.put(name, createComponent(StageDeclaration, closure).with { it.name = name; it })
    }

    def execute(Object delegate) {
        super.execute(delegate)
        this.options.forEach {
            executeOn(it, delegate)
        }
        this.agent?.execute(delegate)
        executeOn(this.parameters, delegate)
        executeOn(this.triggers, delegate)
        this.stages.entrySet().forEach { e ->
            e.value.execute(delegate)
        }
        this.post?.execute(delegate)
    }

}
