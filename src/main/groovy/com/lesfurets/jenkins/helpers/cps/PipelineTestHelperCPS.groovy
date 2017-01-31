package com.lesfurets.jenkins.helpers.cps

import org.codehaus.groovy.control.CompilerConfiguration
import org.codehaus.groovy.control.customizers.ImportCustomizer

import com.cloudbees.groovy.cps.*
import com.cloudbees.groovy.cps.impl.CpsCallableInvocation
import com.lesfurets.jenkins.helpers.PipelineTestHelper

class PipelineTestHelperCPS extends PipelineTestHelper {

    protected Class scriptBaseClass = MockPipelineScriptCPS.class

    private GroovyScriptEngine gse

    protected parallelInterceptor = { Map m ->
        // If you have many steps in parallel and one of the step in Jenkins fails, the other tasks keep runnning in Jenkins.
        // Since here the parallel steps are executed sequentially, we are hiding the error to let other steps run
        // and we make the job failing at the end.
        List<String> exceptions = []
        m.forEach { String parallelName, Closure closure ->
            try {
                def result
                try {
                    result = closure.call()
                } catch(CpsCallableInvocation e) {
                    result = e.invoke(null, null, Continuation.HALT).run().yield.replay()
                }
                return result
            } catch (e) {
                delegate.binding.currentBuild.result = 'FAILURE'
                exceptions.add("$parallelName - ${e.getMessage()}")
            }
        }
        if (exceptions) {
            throw new Exception(exceptions)
        }
    }

    /**
     * Method interceptor for any method called in executing script.
     * Calls are logged on the call stack.
     */
    protected methodInterceptor = { String name, args ->
        // register method call to stack
        int depth = Thread.currentThread().stackTrace.findAll { it.className == delegate.class.name }.size()
        this.registerMethodCall(delegate, depth, name, args)
        // check if it is to be intercepted
        def intercepted = this.getAllowedMethodEntry(name, args)
        if (intercepted != null && intercepted.value) {
            intercepted.value.delegate = delegate
            return intercepted.value.call(*args)
        }
        // if not search for the method declaration
        MetaMethod m = delegate.metaClass.getMetaMethod(name, *args)

        // Fix for GString - String incompatibility in method invocation
        def argsWithoutGstring = args?.collect { it instanceof GString ? it as String : it }?.toArray()
        // ...and call it. If we cannot find it, delegate call to methodMissing
        def result
        if (m) {
            // Call cps steps until it yields a result
            try {
                result = m.invoke(delegate, *argsWithoutGstring)
            } catch (CpsCallableInvocation e) {
                result = e.invoke(null, null, Continuation.HALT).run().yield.replay()
            }
        } else {
            result = delegate.metaClass.invokeMissingMethod(delegate, name, args)
        }
        return result
    }

    PipelineTestHelperCPS build() {
        ImportCustomizer customizer = new ImportCustomizer()
        imports.each { k, v -> customizer.addImport(k, v) }

        CompilerConfiguration configuration = new CompilerConfiguration()
        configuration.setDefaultScriptExtension(scriptExtension)
        configuration.setScriptBaseClass(scriptBaseClass.getName())
        configuration.addCompilationCustomizers(customizer)
        // Add transformer for CPS compilation
        configuration.addCompilationCustomizers(new CpsTransformer())

        GroovyClassLoader cLoader = new GroovyClassLoader(baseClassloader, configuration)
        gse = new GroovyScriptEngine(scriptRoots, cLoader)

        gse.setConfig(configuration)
        this.registerAllowedMethod("parallel", [Map.class], parallelInterceptor)
        return this
    }

    /**
     * Load and run script with given binding context
     * @param scriptName path of the script
     * @param binding
     * @return loaded and run script
     */
    Script loadScript(String scriptName, Binding binding) {
        Objects.requireNonNull(binding)
        binding.setVariable("_TEST_HELPER", this)
        Script script = gse.createScript(scriptName, binding)
        script.metaClass.invokeMethod = methodInterceptor
        script.metaClass.static.invokeMethod = methodInterceptor
        // Probably unnecessary
        try {
            script.run()
        } catch (CpsCallableInvocation inv) {
            println inv
        }
        return script
    }

    /**
     * Marshall - Unmarshall object to test serializability
     * @param object to marshall
     * @return unmarshalled object
     */
    def <T> T roundtripSerialization(T object) {
        def baos = new ByteArrayOutputStream()
        new ObjectOutputStream(baos).writeObject(object)

        def ois = new ObjectInputStreamWithLoader(
                        new ByteArrayInputStream(baos.toByteArray()),
                        this.gse.groovyClassLoader)
        return (T) ois.readObject()
    }

}
