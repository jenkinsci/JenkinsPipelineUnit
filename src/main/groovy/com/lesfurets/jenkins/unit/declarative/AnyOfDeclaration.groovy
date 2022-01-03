package com.lesfurets.jenkins.unit.declarative

import static com.lesfurets.jenkins.unit.declarative.GenericPipelineDeclaration.executeWith

import static groovy.lang.Closure.DELEGATE_FIRST

class AnyOfDeclaration extends WhenDeclaration {

    List<Tuple2<String,ComparatorEnum>> tags = []
    List<Tuple2<String,ComparatorEnum>> branches = []
    List<ChangeRequestDeclaration> changeRequests = []
    List<Boolean> expressions = []
    List<AllOfDeclaration> allOfs = []

    def tag(String pattern) {
        this.tags.add(new Tuple2(pattern, ComparatorEnum.GLOB))
    }

    def tag(Map args) {
        if (args.comparator) {
            ComparatorEnum comparator = ComparatorEnum.getComparator(args.comparator as String)
            this.tags.add(new Tuple2(args.pattern as String, comparator))
        }
        else {
            tag(args.pattern)
        }
    }

    def branch(String pattern) {
        this.branches.add(new Tuple2(pattern, ComparatorEnum.GLOB))
    }

    def branch(Map args) {
        if (args.comparator) {
            ComparatorEnum comparator = ComparatorEnum.getComparator(args.comparator as String)
            this.branches.add(new Tuple2(args.pattern as String, comparator))
        }
        else {
            branch(args.pattern)
        }
    }

    def changeRequest(Object val) {
        this.changeRequests.add(new ChangeRequestDeclaration(val))
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

        if (tags) {
            tags.each { tag ->
                results.add(compareStringToPattern(delegate.env.TAG_NAME, tag))
            }
        }

        if (branches) {
            branches.each { branch ->
                results.add(compareStringToPattern(delegate.env.BRANCH_NAME, branch))
            }
        }

        if (changeRequests) {
            changeRequests.each { changeRequest ->
                results.add(changeRequest.execute(delegate))
            }
        }

        if (expressions) {
            results.add(expressions(delegate))
        }

        if (allOfs) {
            results.addAll(allOf(delegate))
        }

        return results.any()
    }

}
