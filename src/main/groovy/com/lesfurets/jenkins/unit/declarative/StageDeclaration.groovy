package com.lesfurets.jenkins.unit.declarative

import static com.lesfurets.jenkins.unit.declarative.DeclarativePipeline.executeWith
import static groovy.lang.Closure.*

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

    def when(@DelegatesTo(strategy = DELEGATE_ONLY, value = WhenDeclaration) Closure closure) {
        this.when = DeclarativePipeline.createComponent(WhenDeclaration, closure)
    }

    def execute(Object delegate) {
        String name = this.name
        if (!when || when.execute(delegate)) {
            super.execute(delegate)
            // TODO handle credentials
            Closure stageBody = { agent?.execute(delegate) } >> steps.rehydrate(delegate, delegate, delegate)
            Closure cl = { stage("$name", stageBody) }
            executeWith(delegate, cl)
            if (post) {
                this.post.execute(delegate)
            }
        } else {
            executeWith(delegate, { echo "Skipping stage $name" })
        }
    }

}
