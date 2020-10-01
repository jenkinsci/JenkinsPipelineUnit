package com.lesfurets.jenkins.unit.declarative


import static com.lesfurets.jenkins.unit.declarative.GenericPipelineDeclaration.createComponent
import static com.lesfurets.jenkins.unit.declarative.GenericPipelineDeclaration.executeWith
import static groovy.lang.Closure.*

class StageDeclaration {

    AgentDeclaration agent
    Closure environment
    String name
    Closure steps
    WhenDeclaration when
    ParallelDeclaration parallel
    PostDeclaration post
    boolean failFast = false
    List<Closure> options = []
    Map<String, StageDeclaration> stages = [:]

    StageDeclaration(String name) {
        this.name = name
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

    def post(@DelegatesTo(strategy = DELEGATE_FIRST, value = PostDeclaration) Closure closure) {
        this.post = createComponent(PostDeclaration, closure)
    }

    def steps(Closure closure) {
        this.steps = closure
    }

    def stages(@DelegatesTo(DeclarativePipeline) Closure closure) {
        closure.call()
    }

    def failFast(boolean failFast) {
        this.failFast = failFast
    }

    def parallel(@DelegatesTo(strategy = DELEGATE_FIRST, value = ParallelDeclaration) Closure closure) {
        this.parallel = createComponent(ParallelDeclaration, closure).with { it.failFast = failFast; it }
    }

    def when(@DelegatesTo(strategy = DELEGATE_FIRST, value = WhenDeclaration) Closure closure) {
        this.when = createComponent(WhenDeclaration, closure)
    }

    def options(@DelegatesTo(StageDeclaration) Closure closure) {
        options.add(closure)
    }

    def execute(Object delegate) {
        Map envValuestoRestore = [:]

        String name = this.name
        this.options.each {
            executeWith(delegate, it)
        }
        if(parallel) {
            parallel.execute(delegate)
        }

        if (delegate.currentBuild.result == "FAILURE") {
            executeWith(delegate, { echo "Stage \"$name\" skipped due to earlier failure(s)" })
            return
        }

        if (!when || when.execute(delegate)) {
            // Set environment for stage
            if (this.environment) {
                Binding subBinding = new Binding()
                subBinding.metaClass.invokeMissingProperty = { propertyName ->
                    delegate.getProperty(propertyName)
                }
                subBinding.metaClass.setProperty = { String propertyName, Object newValue ->
                    if(delegate.hasProperty(propertyName)){
                        envValuestoRestore.put(propertyName, delegate.getProperty(propertyName))
                    }
                    (delegate.env as Map).put(propertyName, newValue)
                }
                def envClosure = this.environment.rehydrate(subBinding, delegate, this)
                envClosure.resolveStrategy = DELEGATE_FIRST
                envClosure.call()
            }

            // TODO handle credentials
            this.stages.entrySet().forEach { stageEntry ->
                stageEntry.getValue().execute(delegate)
            }
            if(steps) {
                Closure stageBody = { agent?.execute(delegate) } >> steps.rehydrate(delegate, this, this)
                Closure cl = { stage("$name", stageBody) }
                executeWith(delegate, cl)
            }
            if (post) {
                this.post.execute(delegate)
            }
        } else {
            executeWith(delegate, { echo "Skipping stage $name" })
        }
        envValuestoRestore.entrySet().forEach { entry ->
            def envMap = delegate.env as Map
            envMap.put(entry.getKey(), entry.getValue())
        }
    }

}
