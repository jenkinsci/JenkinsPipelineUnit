package com.lesfurets.jenkins.unit.declarative

import static com.lesfurets.jenkins.unit.declarative.DeclarativePipeline.executeWith

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
            executeWith(delegate, this.always)
        }

        switch (currentBuild) {
            case 'SUCCESS':
                executeWith(delegate, this.success)
                break
            case 'FAILURE':
                executeWith(delegate, this.failure)
                break
            case 'ABORTED':
                executeWith(delegate, this.aborted)
                break
            case 'UNSTABLE':
                executeWith(delegate, this.unstable)
                break
        }

        if(currentBuild != previousBuild && this.changed)
        {
            executeWith(delegate, this.changed)
        }
        if(currentBuild != 'SUCCESS' && this.unsuccessful)
        {
            executeWith(delegate, this.unsuccessful)
        }
        if(this.fixed){
            if(currentBuild == 'SUCCESS' && (previousBuild == 'FAILURE' || previousBuild == 'UNSTABLE'))
            {
                executeWith(delegate, this.fixed)
            }
        }
        if(this.regression)
        {
            if((currentBuild == 'FAILURE' || currentBuild == 'UNSTABLE') && previousBuild == 'SUCCESS'){
                executeWith(delegate, this.regression)
            }
        }

        // Cleanup is always performed last
        if(this.cleanup){
            executeWith(delegate, this.cleanup)
        }
    }

}
