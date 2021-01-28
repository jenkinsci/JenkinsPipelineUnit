package com.lesfurets.jenkins.unit.declarative

import org.springframework.util.AntPathMatcher

import java.util.regex.Pattern

import static groovy.lang.Closure.DELEGATE_FIRST

class WhenDeclaration extends GenericPipelineDeclaration {

    AnyOfDeclaration anyOf
    NotDeclaration not
    Boolean buildingTag
    String branch
    String tag
    Closure<Boolean> expression

    private static Pattern getPatternFromGlob(String glob) {
        // from https://stackoverflow.com/a/3619098
        return Pattern.compile('^' + Pattern.quote(glob).replace('*', '\\E.*\\Q').replace('?', '\\E.\\Q') + '$');
    }

    def anyOf(@DelegatesTo(strategy = DELEGATE_FIRST, value = AnyOfDeclaration) Closure closure) {
        this.anyOf = createComponent(AnyOfDeclaration, closure)
    }

    def not(@DelegatesTo(strategy = DELEGATE_FIRST, value = NotDeclaration) Closure closure) {
        this.not = createComponent(NotDeclaration, closure)
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

    Boolean execute(Object delegate) {
        boolean expressionCheck = true
        boolean branchCheck = true
        boolean tagCheck = true
        boolean envCheck = true
        boolean anyOfCheck = true
        boolean notCheck = true

        if (anyOf) {
            anyOfCheck = anyOf.execute(delegate)
        }
        if (not) {
            notCheck = not.execute(delegate)
        }
        if (expression) {
            expressionCheck = executeWith(delegate, expression)
        }
        if (branch) {
            AntPathMatcher antPathMatcher = new AntPathMatcher();
            branchCheck = antPathMatcher.match(branch, delegate.env.BRANCH_NAME)
        }
        if (buildingTag) {
            tagCheck = delegate?.env?.containsKey("TAG_NAME")
        }
        if (tag) {
            tagCheck = delegate.env.TAG_NAME =~ tag
        }
        if (!(env as Map)?.isEmpty()) {
            def environment = env as Map
            environment.entrySet().forEach { e ->
                envCheck = envCheck && (delegate.env."${e.key}" == e.value)
            }
        }

        return expressionCheck && branchCheck && tagCheck && envCheck && anyOfCheck && notCheck
    }

}
