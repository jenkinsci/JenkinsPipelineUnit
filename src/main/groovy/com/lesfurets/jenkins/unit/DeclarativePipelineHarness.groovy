package com.lesfurets.jenkins.unit

class DeclarativePipelineHarness {
    BasePipelineTest base

    Map<String, CallStack> callStacks = [:]
    Map<String, IdentifiedCallStack<?>> identifiedCallStacks = [:]

    DeclarativePipelineHarness(base) {
        this.base = base
        registerCallStack('pipeline')
        registerCallStack('options')
        registerCallStack('parameters')
        registerCallStack('environment')
        registerCallStack('triggers')
        registerCallStack('tools')
        registerCallStack('input')

        registerCallStack('agent')
        registerCallStack('docker')
        registerCallStack('dockerfile')
        registerCallStack('node')

        registerCallStack('stages')
        registerCallStack('parallel')
        registerCallStack('script')
        registerCallStack('steps')

        registerCallStack('when')
        registerCallStack('allOf')
        registerCallStack('anyOf')
        registerCallStack('expression')

        //Register post methods
        registerCallStack('post')
        registerCallStack('always')
        registerCallStack('cleanup')
        registerCallStack('success')
        registerCallStack('failure')
        registerCallStack('regression')
        registerCallStack('changed')
        registerCallStack('fixed')
        registerCallStack('aborted')
        registerCallStack('unstable')
        registerCallStack('unsuccessful')

        registerIdentifiedCallStack('stage', String.class)
        registerIdentifiedCallStack('node', String.class)
        registerIdentifiedCallStack('withCredentials', Object.class)
        registerIdentifiedCallStack('withEnv', List.class)
        registerIdentifiedCallStack('dir', String.class)
    }

    void runCall(String name, Integer idx = 0, Boolean doClear = true) {
        CallStack callStack = callStacks[name]
        if(callStack== null) {
            throw new RuntimeException("No such call stack ${name}")
        }
        callStack.run(idx, doClear)
    }

    public <T> void runIdentified(String name, T value, Boolean doClear = true) {
        IdentifiedCallStack<Object> callStack = identifiedCallStacks[name]
        if(callStack== null) {'pipeline'
            throw new RuntimeException("No such call stack ${name}")
        }
        callStack.run(value, doClear)
    }

    Object runIdentifiedByIndex(String name, Integer idx = 0, Boolean doClear = true) {
        IdentifiedCallStack<Object> callStack = identifiedCallStacks[name]
        if(callStack== null) {
            throw new RuntimeException("No such call stack ${name}")
        }
        return callStack.run(idx, doClear)
    }

    void registerCallStack(String name) {
        callStacks.put(name, new CallStack(name, base.helper))
    }

    public <T> void registerIdentifiedCallStack(String name, Class<T> clazz) {
        identifiedCallStacks.put(name, new IdentifiedCallStack<T>(name, base.helper, clazz))
    }

    void clearAllCalls() {
        base.helper.clearCallStack()
        callStacks.values().forEach { it.clearCalls() }
        identifiedCallStacks.values().forEach { it.clearCalls() }
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

    class IdentifiedCallStack<T> {
        String name
        PipelineTestHelper helper
        Class<T> clazz

        List<T> values = []
        List<Closure> calls = []
        IdentifiedCallStack(name, helper, clazz) {
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
