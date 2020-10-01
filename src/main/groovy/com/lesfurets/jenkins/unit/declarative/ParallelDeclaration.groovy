package com.lesfurets.jenkins.unit.declarative

import static com.lesfurets.jenkins.unit.declarative.GenericPipelineDeclaration.createComponent
import static groovy.lang.Closure.DELEGATE_FIRST

class ParallelDeclaration {

    Map<String, StageDeclaration> stages = [:]
    boolean failFast

    ParallelDeclaration(boolean failFast) {
        this.failFast = failFast
    }

    ParallelDeclaration() {
        this.failFast = false
    }

    def stage(String name,
              @DelegatesTo(strategy = DELEGATE_FIRST, value = StageDeclaration) Closure closure) {
        this.stages.put(name, createComponent(StageDeclaration, closure).with{it.name = name;it} )
    }

    def execute(Object delegate) {
        this.stages.entrySet().forEach { e ->
            e.value.execute(delegate)
        }
    }

}
