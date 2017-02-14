package com.lesfurets.jenkins.unit

abstract class MockPipelineScript extends Script {

    def methodMissing(String name, args) {
        if (this._TEST_HELPER.isMethodAllowed(name, args)) {
            def result = null
            if (args != null) {
                for (argument in args) {
                    result = callIfClosure(argument, result)
                    if (argument instanceof Map) {
                        argument.each { k, v ->
                            result = callIfClosure(k, result)
                            result = callIfClosure(v, result)
                        }
                    }
                }
            }
            return result
        } else {
            throw new MissingMethodException(name, this.class, args)
        }
    }

    def callIfClosure(Object closure, Object currentResult) {
        if (closure instanceof Closure) {
            currentResult = closure.call()
        }
        return currentResult
    }

}
