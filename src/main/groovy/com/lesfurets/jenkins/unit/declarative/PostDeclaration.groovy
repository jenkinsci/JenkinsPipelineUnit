package com.lesfurets.jenkins.unit.declarative

import static com.lesfurets.jenkins.unit.declarative.DeclarativePipeline.executeOn

class PostDeclaration {

    Closure always
    Closure changed
    Closure success
    Closure unstable
    Closure failure
    Closure aborted
    Closure unsuccessful
    Closure cleanup
    Closure fixed
    Closure regression

    def always(Closure closure) {
        this.always = closure
    }

    def changed(Closure closure) {
        this.changed = closure
    }

    def success(Closure closure) {
        this.success = closure
    }

    def unstable(Closure closure) {
        this.unstable = closure
    }
    
    def unsuccessful(Closure closure) {
        this.unsuccessful = closure
    }

    def failure(Closure closure) {
        this.failure = closure
    }

    def aborted(Closure closure) {
        this.aborted = closure
    }

    def cleanup(Closure closure) {
        this.cleanup = closure
    }

    def fixed(Closure closure) {
        this.fixed = closure
    }

    def regression(Closure closure){
        this.regression = closure
    }

    def execute(Object delegate) {
        def currentBuild = delegate.currentBuild.result
        def previousBuild = delegate.currentBuild?.previousBuild?.result
        if (this.always) {
            executeOn(delegate, this.always)
        }

        switch (currentBuild) {
            case 'SUCCESS':
                executeOn(delegate, this.success)
                break
            case 'FAILURE':
                executeOn(delegate, this.failure)
                break
            case 'ABORTED':
                executeOn(delegate, this.aborted)
                break
            case 'UNSTABLE':
                executeOn(delegate, this.unstable)
                break
        }

        if(currentBuild != previousBuild && this.changed)
        {
            executeOn(delegate, this.changed)
        }
        if(currentBuild != 'SUCCESS' && this.unsuccessful)
        {
            executeOn(delegate, this.unsuccessful)
        }
        if(this.fixed){
            if(currentBuild == 'SUCCESS' && (previousBuild == 'FAILURE' || previousBuild == 'UNSTABLE'))
            {
                executeOn(delegate, this.fixed)
            }
        }
        if(this.regression)
        {
            if((currentBuild == 'FAILURE' || currentBuild == 'UNSTABLE') && previousBuild == 'SUCCESS'){
                executeOn(delegate, this.regression)
            }
        }

        // Cleanup is always performed last
        if(this.cleanup){
            executeOn(delegate, this.cleanup)
        }
    }

}
