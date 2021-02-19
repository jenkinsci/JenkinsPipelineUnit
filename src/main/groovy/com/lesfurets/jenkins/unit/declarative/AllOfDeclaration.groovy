package com.lesfurets.jenkins.unit.declarative

import org.springframework.util.AntPathMatcher

import static groovy.lang.Closure.DELEGATE_FIRST

class AllOfDeclaration extends WhenDeclaration {

    List<String> branches = []
    List<Boolean> expressions = []
    List<AnyOfDeclaration> anyOfs = []

    def branch(String name) {
        this.branches.add(name)
    }

    def expression(Closure closure) {
        this.expressions.add(closure)
    }

    def anyOf(@DelegatesTo(strategy = DELEGATE_FIRST, value = AnyOfDeclaration) Closure closure) {
        this.anyOfs.add(createComponent(AnyOfDeclaration, closure))
    }

    def expressions(Object delegate) {
        return this.expressions.collect {executeWith(delegate, it)}.every()
    }

    def anyOf(Object delegate) {
        return this.anyOfs.collect {it.execute(delegate)}
    }

    Boolean execute(Object delegate) {
        def results = []

        AntPathMatcher antPathMatcher = new AntPathMatcher()

        if (this.branches.size() > 0) {
            branches.each { branch ->
                results.add(antPathMatcher.match(branch, delegate.env.BRANCH_NAME))
            }
        }

        if (this.expressions.size() > 0) {
            results.add(expressions(delegate))
        }

        if (this.anyOfs.size() > 0) {
            results.addAll(anyOf(delegate))
        }

        return results.every()
    }
}
