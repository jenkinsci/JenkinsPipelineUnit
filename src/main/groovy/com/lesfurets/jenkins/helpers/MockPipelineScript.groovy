package com.lesfurets.jenkins.helpers

abstract class MockPipelineScript extends Script {

    def methodMissing(String name, args) {
        if (this._TEST_HELPER.isMethodAllowed(name, args)) {
            def result = null
            if (args != null) {
                for (it in args) {
                    if (it instanceof Closure) {
                        result = it.call()
                    }
                    if (it instanceof Map) {
                        it.each { k, v ->
                            if (k instanceof Closure) {
                                result = k.call()
                            }
                            if (v instanceof Closure) {
                                result = v.call()
                            }
                        }
                    }
                }
            }
            return result
        } else {
            throw new MissingMethodException(name, this.class, args)
        }
    }
}
