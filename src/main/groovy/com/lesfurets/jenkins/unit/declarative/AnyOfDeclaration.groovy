package com.lesfurets.jenkins.unit.declarative

import static com.lesfurets.jenkins.unit.declarative.GenericPipelineDeclaration.executeWith
import org.springframework.util.AntPathMatcher

import static groovy.lang.Closure.DELEGATE_FIRST


class AnyOfDeclaration extends WhenDeclaration {

    List<String> tags = []
    List<String> branches = []
    List<Boolean> expressions = []
    List<AllOfDeclaration> allOfs = []

    def tag(String name) {
        this.tags.add(name)
    }

    def branch(String name) {
        this.branches.add(name)
    }

    def expression(Closure closure) {
        this.expressions.add(closure)
    }

    def allOf(@DelegatesTo(strategy = DELEGATE_FIRST, value = AllOfDeclaration) Closure closure) {
        this.allOfs.add(createComponent(AllOfDeclaration, closure))
    }

    def allOf(Object delegate) {
        return this.allOfs.collect {it.execute(delegate)}
    }

    def expressions(Object delegate) {
        return this.expressions.collect {executeWith(delegate, it)}.any()
    }

    Boolean execute(Object delegate) {
        def results = []

        AntPathMatcher antPathMatcher = new AntPathMatcher()

        if (this.tags.size() > 0) {
            tags.each { tag ->
                results.add(antPathMatcher.match(tag, delegate.env.TAG_NAME))
            }
        }

        if (this.branches.size() > 0) {
            branches.each { branch ->
                results.add(antPathMatcher.match(branch, delegate.env.BRANCH_NAME))
            }
        }

        if (this.expressions.size() > 0) {
            results.add(expressions(delegate))
        }

        if (this.allOfs.size() > 0) {
            results.addAll(allOf(delegate))
        }

        return results.any()
    }
}
