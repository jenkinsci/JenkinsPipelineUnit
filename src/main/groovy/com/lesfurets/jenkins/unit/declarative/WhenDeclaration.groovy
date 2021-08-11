package com.lesfurets.jenkins.unit.declarative

import org.springframework.util.AntPathMatcher

import java.util.regex.Pattern

import static groovy.lang.Closure.DELEGATE_FIRST

class WhenDeclaration extends GenericPipelineDeclaration {

    AllOfDeclaration allOf
    AnyOfDeclaration anyOf
    NotDeclaration not
    Boolean buildingTag
    String branch
    ChangeRequestDeclaration changeRequest
    String tag
    Closure<Boolean> expression
    String envName
    String envValue

    protected static Pattern getPatternFromGlob(String glob) {
        // from https://stackoverflow.com/a/3619098
        return Pattern.compile('^' + Pattern.quote(glob).replace('*', '\\E.*\\Q').replace('?', '\\E.\\Q') + '$')
    }

    def allOf(@DelegatesTo(strategy = DELEGATE_FIRST, value = AllOfDeclaration) Closure closure) {
        this.allOf = createComponent(AllOfDeclaration, closure)
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

    def changeRequest(Object o) {
        this.changeRequest = new ChangeRequestDeclaration(o)
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

    def environment(Map args) {
        this.envName = args.name as String
        this.envValue = args.value as String
    }

    Boolean execute(Object delegate) {
        boolean expressionCheck = true
        boolean branchCheck = true
        boolean changeRequestCheck = true
        boolean tagCheck = true
        boolean envCheck = true
        boolean allOfCheck = true
        boolean anyOfCheck = true
        boolean notCheck = true

        if (allOf) {
            allOfCheck = allOf.execute(delegate)
        }
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
            AntPathMatcher antPathMatcher = new AntPathMatcher()
            branchCheck = antPathMatcher.match(branch, delegate.env.BRANCH_NAME)
        }
        if (changeRequest) {
            changeRequestCheck = changeRequest.execute(delegate)
        }
        if (buildingTag) {
            tagCheck = delegate?.env?.containsKey("TAG_NAME")
        }
        if (tag) {
            tagCheck = delegate.env.TAG_NAME =~ tag
        }
        if (envName != null) {
            def val = delegate?.env[envName]
            envCheck = (val == envValue)
        }

        return expressionCheck && branchCheck && changeRequestCheck && tagCheck && envCheck && allOfCheck && anyOfCheck && notCheck
    }

}
