package com.lesfurets.jenkins.unit.declarative

import static groovy.lang.Closure.DELEGATE_FIRST

abstract class GenericPipelineDeclaration {

    AgentDeclaration agent
    Closure environment
    Closure tools
    PostDeclaration post
    Map<String, StageDeclaration> stages = [:]
    // Per-instance binding (was a static field, which is not thread-safe when
    // multiple declarative pipelines are executed concurrently). It is threaded
    // explicitly through createComponent so each component tree carries its own binding.
    def binding = null

    static <T> T createComponent(Class<T> componentType,
                                 Object binding,
                                 @DelegatesTo(strategy = DELEGATE_FIRST) Closure<T> closure) {
        // declare componentInstance as final to prevent any multithreaded issues, since it is used inside closure
        final def componentInstance = componentType.newInstance()
        def rehydrate = closure.rehydrate(componentInstance, closure, componentInstance)
        if (componentInstance.hasProperty('binding')) {
            componentInstance.binding = binding
        }
        rehydrate.call()
        return componentInstance
    }

    static <T> T executeOn(@DelegatesTo.Target Object delegate,
                           @DelegatesTo(strategy = DELEGATE_FIRST) Closure<T> closure) {
        if (closure) {
            def cl = closure.rehydrate(delegate, delegate, delegate)
            cl.resolveStrategy = DELEGATE_FIRST
            return cl.call()
        }
        return null
    }

    static <T> T executeWith(@DelegatesTo.Target Object delegate,
                             @DelegatesTo(strategy = DELEGATE_FIRST) Closure<T> closure) {
        if (closure) {
            def cl = closure.rehydrate(delegate, delegate, delegate)
            cl.resolveStrategy = DELEGATE_FIRST
            return cl.call()
        }
        return null
    }

    /**
     * Runs the child stages in declaration order.
     *
     * A failing stage does not abort the loop: as in Jenkins, the remaining stages still get
     * visited and report themselves as skipped (StageDeclaration checks currentBuild.result).
     * The first failure is rethrown once every stage has been visited.
     *
     * Note: iterate with each() rather than forEach(). forEach() coerces the closure into a
     * java.util.function.Consumer proxy, which wraps any checked exception (such as the one
     * thrown by a failing sh mock) in an UndeclaredThrowableException.
     */
    protected void executeStages(Object delegate) {
        Throwable failure = null
        this.stages.values().each { StageDeclaration stage ->
            try {
                stage.execute(delegate)
            } catch (Throwable t) {
                failure = recordFailure(delegate, failure, t)
            }
        }
        if (failure != null) {
            throw failure
        }
    }

    /**
     * Runs the post section, if any, after the body has completed or failed.
     * An exception thrown by the post section itself is recorded like any other failure.
     *
     * @return the failure to rethrow, or null
     */
    protected Throwable executePost(Object delegate, Throwable failure) {
        if (this.post) {
            try {
                this.post.execute(delegate)
            } catch (Throwable t) {
                return recordFailure(delegate, failure, t)
            }
        }
        return failure
    }

    /**
     * Marks the build as failed (the result can only get worse, so ABORTED / NOT_BUILT are kept)
     * and returns the failure that should eventually be rethrown. The first failure wins; later
     * distinct ones are attached to it as suppressed exceptions.
     */
    protected static Throwable recordFailure(Object delegate, Throwable previous, Throwable current) {
        def currentBuild = delegate.binding.variables.currentBuild
        if (currentBuild != null && !(currentBuild.result in ['ABORTED', 'NOT_BUILT'])) {
            currentBuild.result = 'FAILURE'
            currentBuild.currentResult = 'FAILURE'
        }
        if (previous == null) {
            return current
        }
        if (!previous.is(current)) {
            previous.addSuppressed(current)
        }
        return previous
    }

    def agent(Object o) {
        this.agent = new AgentDeclaration().with { it.label = o; it }
    }

    def agent(@DelegatesTo(strategy = DELEGATE_FIRST, value = AgentDeclaration) Closure closure) {
        this.agent = createComponent(AgentDeclaration, this.binding, closure)
    }

    def environment(Closure closure) {
        this.environment = closure
    }

    def tools(Closure closure) {
        this.tools = closure
    }

    def post(@DelegatesTo(strategy = DELEGATE_FIRST, value = PostDeclaration) Closure closure) {
        this.post = createComponent(PostDeclaration, this.binding, closure)
    }

    def stages(@DelegatesTo(DeclarativePipeline) Closure closure) {
        closure.call()
    }

    def stage(String name,
              @DelegatesTo(strategy = DELEGATE_FIRST, value = StageDeclaration) Closure closure) {
        this.stages.put(name, createComponent(StageDeclaration, this.binding, closure).with { it.name = name; it })
    }

    def getProperty(String propertyName) {
        def metaProperty = this.metaClass.getMetaProperty(propertyName)
        if (metaProperty) {
            return metaProperty.getProperty(this)
        } else {
            if (binding?.hasProperty(propertyName) || binding?.hasVariable(propertyName)) {
                return binding.getProperty(propertyName)
            }
            if (binding?.hasVariable("params") && (binding?.getProperty("params") as Map).containsKey(propertyName)) {
                return (binding?.getProperty("params") as Map).get(propertyName)
            }
            if (binding?.hasVariable("env") && (binding?.getProperty("env") as Map).containsKey(propertyName)) {
                return (binding?.getProperty("env") as Map).get(propertyName)
            }
            def metaMethod = this.metaClass.getMetaMethod("propertyMissing", propertyName)
            if (metaMethod) {
                metaMethod.invoke(this, propertyName)
            } else {
                throw new MissingPropertyException(propertyName)
            }
        }
    }

    def execute(Object delegate) {
        // set environment
        if (this.environment) {
            def env = delegate.binding.env
            // let access env and currentBuild properties in environment closure
            env.env = env
            env.currentBuild = delegate.binding.currentBuild

            def cl = this.environment.rehydrate(wrapEnv(env as Map<String, Object>), delegate, this)
            cl.resolveStrategy = DELEGATE_FIRST
            cl.call()
        }
    }

    private def wrapEnv(Map<String, Object> env) {
        return new GroovyObject() {
            Object invokeMethod(String name, Object args) {
                GenericPipelineDeclaration.this.invokeMethod(name, args)
            }

            Object getProperty(String propertyName) {
                if (env.containsKey(propertyName)) {
                    env.get(propertyName)
                } else {
                    GenericPipelineDeclaration.this.getProperty(propertyName)
                }
            }

            void setProperty(String propertyName, Object newValue) {
                env.put(propertyName, newValue)
            }

            MetaClass getMetaClass() {
                GenericPipelineDeclaration.this.metaClass
            }

            void setMetaClass(MetaClass metaClass) {
                throw new UnsupportedOperationException("This is wrapper object. Setting metadata class is not supported here.")
            }
        }
    }

}
