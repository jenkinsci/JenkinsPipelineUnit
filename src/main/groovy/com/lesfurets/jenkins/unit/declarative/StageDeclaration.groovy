package com.lesfurets.jenkins.unit.declarative


import static groovy.lang.Closure.*

class StageDeclaration extends GenericPipelineDeclaration {

    String name
    Closure steps
    WhenDeclaration when
    ParallelDeclaration parallel
    boolean failFast = false
    List<Closure> options = []

    StageDeclaration(String name) {
        this.name = name
    }

    def steps(Closure closure) {
        this.steps = closure
    }

    def failFast(boolean failFast) {
        this.failFast = failFast
    }

    def getBinding_var() {
        return binding?.var
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
        String name = this.name
        def actions = 0
        if(parallel) {
            actions++
        }
        if(stages.size()>0) {
            actions++
        }
        if(steps) {
            actions++
        }
        if (actions > 1 ) {
            throw new IllegalArgumentException ("""Only one of "matrix", "parallel", "stages", or "steps" allowed for stage "${name}" """)
        }

        this.options.each {
            executeOn(delegate, it)
        }

        if(parallel) {
            parallel.execute(delegate)
        }

        if(delegate.binding.variables.currentBuild.result == "FAILURE"){
            executeWith(delegate, { echo "Stage \"$name\" skipped due to earlier failure(s)" })
            return
        }

        if (!when || when.execute(delegate)) {
            super.execute(delegate)

            // TODO handle credentials

            Closure stageBody = { agent?.execute(delegate) }
            Closure cl = { stage("$name", stageBody) }
            if(steps) {
                stageBody =  stageBody >> steps.rehydrate(delegate, this, delegate)
            }
            executeWith(delegate, cl)

            this.stages.entrySet().forEach { e ->
                e.value.execute(delegate)
            }

            if (post) {
                this.post.execute(delegate)
            }
        } else {
            executeWith(delegate, { echo "Skipping stage $name" })
        }
    }

}
