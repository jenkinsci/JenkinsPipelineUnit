package com.lesfurets.jenkins.unit.cps

import org.codehaus.groovy.control.CompilerConfiguration
import org.codehaus.groovy.control.customizers.ImportCustomizer

import com.cloudbees.groovy.cps.Continuation
import com.cloudbees.groovy.cps.CpsTransformer
import com.cloudbees.groovy.cps.Envs
import com.cloudbees.groovy.cps.ObjectInputStreamWithLoader
import com.cloudbees.groovy.cps.impl.CpsCallableInvocation
import com.cloudbees.groovy.cps.impl.CpsClosure
import com.lesfurets.jenkins.unit.InterceptingGCL
import com.lesfurets.jenkins.unit.PipelineTestHelper
import com.lesfurets.jenkins.unit.global.lib.LibraryAnnotationTransformer
import com.lesfurets.jenkins.unit.global.lib.LibraryLoader

class PipelineTestHelperCPS extends PipelineTestHelper {

    protected Class scriptBaseClass = MockPipelineScriptCPS.class


    /**
     * Method interceptor for any method called in executing script.
     * Calls are logged on the call stack.
     */
    public methodInterceptor = { String name, Object[] args ->
        // register method call to stack
        int depth = Thread.currentThread().stackTrace.findAll { it.className == delegate.class.name }.size()
        this.registerMethodCall(delegate, depth, name, args)
        // check if it is to be intercepted
        def intercepted = this.getAllowedMethodEntry(name, args)
        if (intercepted != null && intercepted.value) {
            intercepted.value.delegate = delegate
            return invokeInterceptedClosure(intercepted.value, args)
        }
        // if not search for the method declaration
        MetaMethod m = delegate.metaClass.getMetaMethod(name, args)
        // call it. If we cannot find it, delegate call to methodMissing
        def result
        if (m) {
            // Call cps steps until it yields a result
            try {
                result = m.doMethodInvoke(delegate, args)
            } catch (CpsCallableInvocation e) {
                result = e.invoke(null, null, Continuation.HALT).run().yield.replay()
            }
        } else {
            result = delegate.metaClass.invokeMissingMethod(delegate, name, args)
        }
        return result
    }

    def getMethodInterceptor() {
        return methodInterceptor
    }

    PipelineTestHelperCPS init() {
        CompilerConfiguration configuration = new CompilerConfiguration()
        GroovyClassLoader cLoader = new InterceptingGCL(this, baseClassloader, configuration)

        libLoader = new LibraryLoader(cLoader, libraries)
        LibraryAnnotationTransformer libraryTransformer = new LibraryAnnotationTransformer(libLoader)
        configuration.addCompilationCustomizers(libraryTransformer)

        ImportCustomizer importCustomizer = new ImportCustomizer()
        imports.each { k, v -> importCustomizer.addImport(k, v) }
        configuration.addCompilationCustomizers(importCustomizer)
        // Add transformer for CPS compilation
        configuration.addCompilationCustomizers(new CpsTransformer())

        configuration.setDefaultScriptExtension(scriptExtension)
        configuration.setScriptBaseClass(scriptBaseClass.getName())

        gse = new GroovyScriptEngine(scriptRoots, cLoader)
        gse.setConfig(configuration)
        this.registerAllowedMethod("parallel", [Map.class], parallelInterceptor)
        return this
    }

    @Override
    protected Object runScript(Script script) {
        try {
            return script.run()
        } catch (CpsCallableInvocation inv) {
            return inv.invoke(null, null, Continuation.HALT).run().yield.replay()
        }
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

    /**
     * Call closure using Continuation steps of groovy CPS
     * At each step whole environment of the step is verified against serializability
     *
     * @param closure to call
     * @param args array of arguments passed to this closure call. Is null by default.
     * @return result of the closure call
     */
    @Override
    Object callClosure(Closure closure, Object[] args = null) {
        try {
            if (args) {
                return closure.call()
            } else {
                return closure.call(args)
            }
        } catch(CpsCallableInvocation e) {
            def next = e.invoke(Envs.empty(), null, Continuation.HALT)
            while(next.yield==null) {
                try {
                    this.roundtripSerialization(next.e)
                } catch (exception) {
                    throw new Exception(next.e.toString(), exception)
                }
                next = next.step()
            }
            return next.yield.replay()
        }
    }
}
