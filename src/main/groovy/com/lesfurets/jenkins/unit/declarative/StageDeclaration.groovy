package com.lesfurets.jenkins.unit.declarative

class StageDeclaration extends GenericPipelineDeclaration {

    String name
    Closure steps
    WhenDeclaration when

    StageDeclaration(String name) {
        this.name = name
    }

    def steps(Closure closure) {
        this.steps = closure
    }

    def when(Closure closure) {
        this.when = DeclarativePipeline.createComponent(WhenDeclaration, closure)
    }

    def execute(Object delegate) {
        if (!when || when.execute(delegate)) {
            super.execute(delegate)
            // TODO handle credentials
            Closure inside = { agent?.execute(delegate) } >> steps.rehydrate(delegate, this, this)
            Closure cl = { stage("$name", inside) }
            def stepsCl = cl.rehydrate(delegate, this, this)
            stepsCl.resolveStrategy = Closure.DELEGATE_FIRST
            stepsCl.call()
            if (post) {
                this.post.execute(delegate)
            }
        } else {
            Closure cl = { echo "Skipping stage $name" }
            cl.rehydrate(delegate, this, this).call()
        }
    }

}
