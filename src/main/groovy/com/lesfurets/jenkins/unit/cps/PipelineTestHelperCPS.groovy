package com.lesfurets.jenkins.unit.cps

import org.codehaus.groovy.control.CompilerConfiguration
import org.codehaus.groovy.control.customizers.ImportCustomizer
import org.codehaus.groovy.runtime.InvokerHelper

import com.cloudbees.groovy.cps.Continuation
import com.cloudbees.groovy.cps.ObjectInputStreamWithLoader
import com.cloudbees.groovy.cps.impl.CpsCallableInvocation
import com.lesfurets.jenkins.unit.PipelineTestHelper
import com.lesfurets.jenkins.unit.global.lib.*

class PipelineTestHelperCPS extends PipelineTestHelper {

    protected Class scriptBaseClass = MockPipelineScriptCPS.class

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
            throw new Exception(exceptions.join(','))
        }
    }

    /**
     * Method interceptor for any method called in executing script.
     * Calls are logged on the call stack.
     */
    public methodInterceptor = { String name, args ->
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
        // call it. If we cannot find it, delegate call to methodMissing
        def result
        if (m) {
            // Call cps steps until it yields a result
            try {
                result = m.doMethodInvoke(delegate, *args)
            } catch (CpsCallableInvocation e) {
                result = e.invoke(null, null, Continuation.HALT).run().yield.replay()
            }
        } else {
            result = delegate.metaClass.invokeMissingMethod(delegate, name, args)
        }
        return result
    }

    PipelineTestHelperCPS build() {
        CompilerConfiguration configuration = new CompilerConfiguration()
        GroovyClassLoader cLoader = new GroovyClassLoader(baseClassloader, configuration)

        libLoader = new LibraryLoader(cLoader, libraries)
        LibraryAnnotationTransformer libraryTransformer = new LibraryAnnotationTransformer(libLoader)
        configuration.addCompilationCustomizers(libraryTransformer)

        ImportCustomizer importCustomizer = new ImportCustomizer()
        imports.each { k, v -> importCustomizer.addImport(k, v) }
        configuration.addCompilationCustomizers(importCustomizer)
        // Add transformer for CPS compilation
        // TODO skip cps transformation for now, there are problems calling accessors
        configuration.addCompilationCustomizers(new LibraryCpsTransformer())
        //  configuration.addCompilationCustomizers(new CpsTransformer())

        configuration.setDefaultScriptExtension(scriptExtension)
        configuration.setScriptBaseClass(scriptBaseClass.getName())

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
        Class scriptClass = gse.loadScriptByName(scriptName)
        libLoader.setGlobalVars(binding, this)
        Script script = InvokerHelper.createScript(scriptClass, binding)
        script.metaClass.invokeMethod = methodInterceptor
        script.metaClass.static.invokeMethod = methodInterceptor
        // Probably unnecessary
        try {
            println "Running ${script.class.name}"
            script.run()
            println "returning ${script.class.name}"
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
