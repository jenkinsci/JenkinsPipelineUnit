package com.lesfurets.jenkins.unit.declarative

import static groovy.lang.Closure.DELEGATE_ONLY
//import static com.lesfurets.jenkins.unit.declarative.DeclarativePipeline.executeOn

class ParallelDeclaration extends GenericPipelineDeclaration {

    boolean failFast

    ParallelDeclaration(boolean failFast) {
        this.failFast = failFast
    }

    ParallelDeclaration() {
        this.failFast = false
    }

    def stage(String name,
              @DelegatesTo(strategy = DELEGATE_ONLY, value = StageDeclaration) Closure closure) {
        this.stages.put(name, createComponent(StageDeclaration, closure).with{it.name = name;it} )
    }

    def execute(Object delegate) {
        super.execute(delegate)
        this.stages.entrySet().forEach { e ->
            e.value.execute(delegate)
        }
    }

}