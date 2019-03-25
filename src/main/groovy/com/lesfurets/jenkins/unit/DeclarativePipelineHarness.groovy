package com.lesfurets.jenkins.unit

class DeclarativePipelineHarness {
    BasePipelineTest base

    Map<String, CallStack> stepCallStacks = [:]
    Map<String, ParameterizedCallStack<?>> parameterizedStepCallStacks = [:]

    Object lastClosureStepParameter = null

    List<String> allowedClosureSteps
    Map<String, Class<Object>> allowedParameterizedClosureSteps

    DeclarativePipelineHarness(BasePipelineTest base, List<String> closureSteps, Map<String, Class<Object>> parameterizedClosureSteps) {
        this.base = base
        this.allowedClosureSteps = closureSteps
        this.allowedParameterizedClosureSteps = parameterizedClosureSteps

        allowedClosureSteps.forEach { it -> registerClosureStep(it)}
        allowedParameterizedClosureSteps.entrySet().forEach { entry -> registerParameterizedClosureStep(entry.key, entry.value)}
    }

    DeclarativePipelineHarness runClosureStep(String name, Integer idx = 0, Boolean doClear = true) {
        CallStack callStack = stepCallStacks[name]
        if(callStack== null) {
            throw new RuntimeException("No such call stack ${name}")
        }
        callStack.run(idx, doClear)
        return this
    }

    public <T> DeclarativePipelineHarness runParameterizedClosureStep(String name, T value, Boolean doClear = true) {
        ParameterizedCallStack<Object> callStack = parameterizedStepCallStacks[name]
        if(callStack== null) {'pipeline'
            throw new RuntimeException("No such call stack ${name}")
        }
        this.lastClosureStepParameter = callStack.run(value, doClear)
        return this
    }

    DeclarativePipelineHarness runParameterizedClosureStepByIndex(String name, Integer idx = 0, Boolean doClear = true) {
        ParameterizedCallStack<Object> callStack = parameterizedStepCallStacks[name]
        if(callStack== null) {
            throw new RuntimeException("No such call stack ${name}")
        }
        this.lastClosureStepParameter = callStack.run(idx, doClear)
        return this
    }

    DeclarativePipelineHarness runStage(String name) {
        return this
                .runClosureStep('pipeline')
                .runClosureStep('stages')
                .runParameterizedClosureStep('stage', name)
    }

    DeclarativePipelineHarness runParallelSubStage(String parentName, String childName) {
        return this
                .runClosureStep('pipeline')
                .runClosureStep('stages')
                .runParameterizedClosureStep('stage', parentName)
                .runClosureStep('parallel')
                .runParameterizedClosureStep('stage', childName)
    }

    Object getLastClosureStepParameterValue() {
        return lastClosureStepParameter
    }

    void registerClosureStep(String name) {
        stepCallStacks.put(name, new CallStack(name, base.helper))
    }

    public <T> void registerParameterizedClosureStep(String name, Class<T> clazz) {
        parameterizedStepCallStacks.put(name, new ParameterizedCallStack<T>(name, base.helper, clazz))
    }

    void clearAllCalls() {
        base.helper.clearCallStack()
        stepCallStacks.values().forEach { it.clearCalls() }
        parameterizedStepCallStacks.values().forEach { it.clearCalls() }
    }


    class CallStack {
        String name
        PipelineTestHelper helper

        List<Closure> calls = []

        CallStack(name, helper) {
            this.name = name
            this.helper = helper

            this.helper.registerAllowedMethod(name, [Closure.class], { calls.add(it) })
        }

        void clearCalls() {
            calls.clear()
        }

        void run(Integer idx = 0, doClear = true) {
            if(! (idx < calls.size())) {
                throw new RuntimeException("not enough calls in stack for ${name}, expected idx: ${idx}, size: ${idx}")
            }
            def c = calls[idx]
            if(doClear) { clearAllCalls() }
            c()
        }
    }

    class ParameterizedCallStack<T> {
        String name
        PipelineTestHelper helper
        Class<T> clazz

        List<T> values = []
        List<Closure> calls = []

        ParameterizedCallStack(name, helper, clazz) {
            this.name = name
            this.helper = helper
            this.clazz = clazz

            this.helper.registerAllowedMethod(name, [clazz, Closure.class], { v, c ->
                values.add(v)
                calls.add(c)
            })
        }

        void clearCalls() {
            values.clear()
            calls.clear()
        }

        T run(Integer idx = 0, doClear = true) {
            if(! (idx < calls.size())) {
                throw new RuntimeException("not enough calls in stack for ${name}, expected idx: ${idx}, size: ${idx}")
            }

            def c = calls[idx]
            if(doClear) { clearAllCalls() }
            c()
            return values[idx]
        }

        void run(T value, doClear = true) {
            def idx = values.findIndexOf { it == value }
            if(idx < 0) {
                throw new RuntimeException("call for $value does not exist, existing values are ${values}")
            }
            def c = calls[idx]
            if(doClear) { clearAllCalls() }
            c()
        }
    }
}
