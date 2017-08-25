package com.lesfurets.jenkins.unit.cps

import com.cloudbees.groovy.cps.Continuation
import com.cloudbees.groovy.cps.CpsTransformer
import com.cloudbees.groovy.cps.Envs
import com.cloudbees.groovy.cps.ObjectInputStreamWithLoader
import com.cloudbees.groovy.cps.TransformerConfiguration
import com.cloudbees.groovy.cps.impl.CpsCallableInvocation
import com.lesfurets.jenkins.unit.PipelineTestHelper

class PipelineTestHelperCPS extends PipelineTestHelper {

    protected Class scriptBaseClass = MockPipelineScriptCPS.class

    @Override
    PipelineTestHelperCPS init() {
        super.init()
        // Set script base class
        gse.getConfig().setScriptBaseClass(scriptBaseClass.getName())
        // Add transformer for CPS compilation
        def transformer = new CpsTransformer()
        transformer.setConfiguration(new TransformerConfiguration().withClosureType(MockClosure.class))
        gse.getConfig().addCompilationCustomizers(transformer)
        return this
    }

    @Override
    protected Object callMethod(MetaMethod method, Object delegate, Object[] args) {
        try {
            super.callMethod(method, delegate, args)
        } catch (CpsCallableInvocation e) {
            return e.invoke(null, null, Continuation.HALT).run().yield.replay()
        }
    }

    @Override
    protected Object runScriptInternal(Script script) {
        try {
            super.runScriptInternal(script)
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
            super.callClosure(closure, args)
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
