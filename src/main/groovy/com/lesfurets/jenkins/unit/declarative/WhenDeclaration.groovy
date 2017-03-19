package com.lesfurets.jenkins.unit.declarative

class WhenDeclaration {

    String branch
    Closure<Boolean> expression
    Map<String, Object> environment = [:]

    def environment(String name, Object value) {
        this.environment.put(name, value)
    }

    def environment(envs) {
        this.environment(envs.name, envs.value)
    }

    def branch (String name) {
        this.branch = name
    }

    def expression(Closure closure) {
        this.expression = closure
    }

    boolean execute(Object delegate) {
        boolean exp = true
        boolean br = true
        boolean env = true
        if (expression) {
            exp = expression.rehydrate(delegate, this, this).call()
        }
        if (branch) {
            br = this.branch == delegate.env.BRANCH_NAME
        }
        if (!environment.isEmpty()) {
            environment.entrySet().forEach { e ->
                env = env && (delegate.env."${e.key}" == e.value)
            }
        }
        return exp && br && env
    }

}