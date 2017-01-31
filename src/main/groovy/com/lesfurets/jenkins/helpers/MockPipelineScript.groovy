package com.lesfurets.jenkins.helpers

abstract class MockPipelineScript extends Script {

    def methodMissing(String name, args) {
        if (this._TEST_HELPER.isMethodAllowed(name, args)) {
            def result = null
            if (args != null) {
                for (argument in args) {
                    callIfClosure(argument)
                    if (argument instanceof Map) {
                        argument.each { k, v ->
                            callIfClosure(k)
                            callIfClosure(v)
                        }
                    }
                }
            }
            return result
        } else {
            throw new MissingMethodException(name, this.class, args)
        }
    }

    def callIfClosure(Object closure) {
        def result = null
        if (closure instanceof Closure) {
            result = closure.call()
        }
        return result
    }

}
