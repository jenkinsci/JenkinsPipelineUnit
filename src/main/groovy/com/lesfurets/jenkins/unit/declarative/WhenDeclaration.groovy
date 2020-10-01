package com.lesfurets.jenkins.unit.declarative

import org.springframework.util.AntPathMatcher

import java.util.regex.Pattern

import static com.lesfurets.jenkins.unit.declarative.GenericPipelineDeclaration.createComponent
import static groovy.lang.Closure.DELEGATE_FIRST

class WhenDeclaration {

    AllOfDeclaration allOf
    AnyOfDeclaration anyOf
    NotDeclaration not
    Boolean buildingTag
    String branch
    String tag
    Closure<Boolean> expression
    String envName
    String envValue

    private static Pattern getPatternFromGlob(String glob) {
        // from https://stackoverflow.com/a/3619098
        return Pattern.compile('^' + Pattern.quote(glob).replace('*', '\\E.*\\Q').replace('?', '\\E.\\Q') + '$')
    }

    def allOf(@DelegatesTo(strategy = DELEGATE_FIRST, value = AllOfDeclaration) Closure closure) {
        this.allOf = createComponent(AllOfDeclaration, closure)
    }

    def anyOf(@DelegatesTo(strategy = DELEGATE_FIRST, value = AnyOfDeclaration) Closure closure) {
        this.anyOfCondition = createComponent(AnyOfDeclaration, closure)
    }

    def not(@DelegatesTo(strategy = DELEGATE_FIRST, value = NotDeclaration) Closure closure) {
        this.notCondition = createComponent(NotDeclaration, closure)
    }

    def branch (String name) {
        this.branchCondition = name
    }

    def environment(Map condition){
        this.environmentCondition = condition
    }

    def tag (String name) {
        this.tagCondition = getPatternFromGlob(name)
    }

    def buildingTag () {
        this.buildingTagCondition = true
    }

    def expression(Closure closure) {
        this.expressionCondition = closure
    }

    def environment(Map args) {
        this.envName = args.name as String
        this.envValue = args.value as String
    }

    Boolean execute(Object delegate) {
        boolean expressionCheck = true
        boolean branchCheck = true
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
        if (notCondition) {
            notCheck = notCondition.execute(delegate)
        }
        if (expressionCondition) {
            expressionCheck = executeWith(delegate, expressionCondition)
        }
        if (branch) {
            AntPathMatcher antPathMatcher = new AntPathMatcher()
            branchCheck = antPathMatcher.match(branch, delegate.env.BRANCH_NAME)
        }
        if (buildingTag) {
            tagCheck = delegate?.env?.containsKey("TAG_NAME")
        }
        if (tagCondition) {
            tagCheck = delegate.env.TAG_NAME =~ tagCondition
        }
        if (envName != null) {
            def val = delegate?.env[envName]
            envCheck = (val == envValue)
        }

        return expressionCheck && branchCheck && tagCheck && envCheck && allOfCheck && anyOfCheck && notCheck
    }

}
