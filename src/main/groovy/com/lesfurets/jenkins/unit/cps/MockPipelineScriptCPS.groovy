package com.lesfurets.jenkins.unit.cps

import com.lesfurets.jenkins.unit.MockPipelineScript

abstract class MockPipelineScriptCPS extends MockPipelineScript implements Serializable {

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
