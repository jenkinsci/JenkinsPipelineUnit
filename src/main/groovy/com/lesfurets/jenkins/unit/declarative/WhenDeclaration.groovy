package com.lesfurets.jenkins.unit.declarative

import java.util.regex.Pattern

import static groovy.lang.Closure.DELEGATE_FIRST

class WhenDeclaration extends GenericPipelineDeclaration {

    protected static Pattern getPatternFromGlob(String glob) {
        // from https://stackoverflow.com/a/3619098
        return Pattern.compile('^' + Pattern.quote(glob).replace('*', '\\E.*\\Q').replace('?', '\\E.\\Q') + '$')
    }

    AllOfDeclaration allOf
    AnyOfDeclaration anyOf
    NotDeclaration not
    Boolean buildingTag
    Tuple2<String,ComparatorEnum> branch
    ChangeRequestDeclaration changeRequest
    Tuple2<String,ComparatorEnum> tag
    Closure<Boolean> expression
    String envName
    String envValue

    def allOf(@DelegatesTo(strategy = DELEGATE_FIRST, value = AllOfDeclaration) Closure closure) {
        this.allOf = createComponent(AllOfDeclaration, closure)
    }

    def anyOf(@DelegatesTo(strategy = DELEGATE_FIRST, value = AnyOfDeclaration) Closure closure) {
        this.anyOf = createComponent(AnyOfDeclaration, closure)
    }

    def not(@DelegatesTo(strategy = DELEGATE_FIRST, value = NotDeclaration) Closure closure) {
        this.not = createComponent(NotDeclaration, closure)
    }

    def branch(String pattern) {
        branch = new Tuple2(pattern, ComparatorEnum.GLOB)
    }

    def branch(Map args) {
        if (args.comparator) {
            ComparatorEnum comparator = ComparatorEnum.getComparator(args.comparator as String)
            branch = new Tuple2(args.pattern as String, comparator)
        }
        else {
            branch(args.pattern)
        }
    }

    def changeRequest(Object o) {
        this.changeRequest = new ChangeRequestDeclaration(o)
    }

    def tag(String pattern) {
        tag = new Tuple2(pattern, ComparatorEnum.GLOB)
    }

    def tag(Map args) {
        if (args.comparator) {
            ComparatorEnum comparator = ComparatorEnum.getComparator(args.comparator as String)
            tag = new Tuple2(args.pattern as String, comparator)
        }
        else {
            tag(args.pattern)
        }
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
            branchCheck = compareStringToPattern(delegate.env.BRANCH_NAME, branch)
        }
        if (changeRequest) {
            changeRequestCheck = changeRequest.execute(delegate)
        }
        if (buildingTag) {
            tagCheck = delegate?.env?.containsKey("TAG_NAME")
        }
        if (tag) {
            tagCheck = compareStringToPattern(delegate.env.TAG_NAME, tag)
        }
        if (envName != null) {
            def val = delegate?.env[envName]
            envCheck = (val == envValue)
        }

        return expressionCheck && branchCheck && changeRequestCheck && tagCheck && envCheck && allOfCheck && anyOfCheck && notCheck
    }

    protected Boolean compareStringToPattern(String string, Tuple2 tuple) {
        return compareStringToPattern(string, tuple.first, tuple.second)
    }

    protected Boolean compareStringToPattern(String string, String pattern, ComparatorEnum comparator) {
        switch (comparator) {
            case ComparatorEnum.EQUALS:
                return string == pattern
            case ComparatorEnum.GLOB:
                return string ==~ getPatternFromGlob(pattern)
            case ComparatorEnum.REGEXP:
                return string ==~ pattern
        }

        throw new IllegalArgumentException("Invalid comparator '${comparator}'")
    }

}
