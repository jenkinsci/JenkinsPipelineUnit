package com.lesfurets.jenkins.unit.cps

import com.cloudbees.groovy.cps.Continuation
import com.cloudbees.groovy.cps.Envs
import com.cloudbees.groovy.cps.impl.CpsCallableInvocation
import com.cloudbees.groovy.cps.impl.CpsClosure
import com.lesfurets.jenkins.unit.MockPipelineScript

abstract class MockPipelineScriptCPS extends MockPipelineScript implements Serializable {

    /**
     * Call closure using Continuation steps of groovy CPS
     * At each step whole environment of the step is verified against serializability
     *
     * @param closure to execute
     * @return result of the closure execution
     */
    def callIfClosure(Object closure) {
        def result = null
        // Every closure we receive here is CpsClosure, NonCPS code does not get called in here.
        if (closure instanceof CpsClosure) {
            try {
                result = closure.call()
            } catch (CpsCallableInvocation e) {
                def next = e.invoke(Envs.empty(), null, Continuation.HALT)
                while(next.yield==null) {
                    try {
                        this._TEST_HELPER.roundtripSerialization(next.e)
                    } catch (exception) {
                        throw new Exception(next.e.toString(), exception)
                    }
                    next = next.step()
                }
                result = next.yield.replay()
            }
        }
        return result
    }

    private void writeObject(ObjectOutputStream oos) throws IOException {
        // binding is defined in non-serializable Script class,
        // so we need to persist that here
        def variables = getBinding().getVariables().clone()
        // Remove injected test helper from variables
        variables.remove("_TEST_HELPER")
        oos.writeObject(variables)
    }

    private void readObject(ObjectInputStream ois) throws IOException, ClassNotFoundException {
        Map m = (Map)ois.readObject()
        getBinding().getVariables().putAll(m)
    }
}
