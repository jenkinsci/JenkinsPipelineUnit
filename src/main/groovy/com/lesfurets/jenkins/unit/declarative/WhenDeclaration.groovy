package com.lesfurets.jenkins.unit.declarative

import static com.lesfurets.jenkins.unit.declarative.DeclarativePipeline.executeWith
import java.util.regex.Pattern

class WhenDeclaration {

    Boolean buildingTag = false
    String branch
    String tag
    Closure<Boolean> expression
    Map<String, Object> environment = [:]

    private static Pattern getPatternFromGlob(String glob) {
        // from https://stackoverflow.com/a/3619098
        return Pattern.compile('^' + Pattern.quote(glob).replace('*', '\\E.*\\Q').replace('?', '\\E.\\Q') + '$');
    }

    def environment(String name, Object value) {
        this.environment.put(name, value)
    }

    def environment(envs) {
        this.environment(envs.name, envs.value)
    }

    def branch (String name) {
        this.branch = name
    }

    def tag (String name) {
        this.tag = getPatternFromGlob(name)
    }

    def buildingTag () {
        this.buildingTag = true
    }

    def expression(Closure closure) {
        this.expression = closure
    }

    boolean execute(Object delegate) {
        boolean exp = true
        boolean br = true
        boolean ta = true
        boolean env = true
        if (expression) {
            exp = executeWith(delegate, expression)
        }
        if (branch) {
            br = this.branch == delegate.env.BRANCH_NAME
        }
        if (buildingTag) {
            ta = delegate.env.containsKey(TAG_NAME)
        }
        if (tag) {
            ta = delegate.env.TAG_NAME =~ tag
        }
        if (!environment.isEmpty()) {
            environment.entrySet().forEach { e ->
                env = env && (delegate.env."${e.key}" == e.value)
            }
        }
        return exp && br && ta && env
    }

}
