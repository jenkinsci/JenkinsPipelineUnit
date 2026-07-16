package com.lesfurets.jenkins.unit.cps;

import com.lesfurets.jenkins.unit.MockPipelineScript;
import java.io.*;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public abstract class MockPipelineScriptCPS extends MockPipelineScript implements Serializable {

    private static final long serialVersionUID = 1L;

    private transient Map<String, Object> cachedVariables;

    private void writeObject(ObjectOutputStream oos) throws IOException {
        Map<String, Object> variables = new HashMap<>(getBinding().getVariables());
        oos.writeObject(variables);
    }

    @SuppressWarnings("unchecked")
    private void readObject(ObjectInputStream ois) throws IOException, ClassNotFoundException {
        Map<String, Object> variables = (Map<String, Object>) ois.readObject();
        getBinding().getVariables().putAll(variables);
        cachedVariables = Collections.synchronizedMap(new HashMap<>(variables));
    }
}
