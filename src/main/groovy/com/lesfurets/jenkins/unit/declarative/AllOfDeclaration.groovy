package com.lesfurets.jenkins.unit.declarative

import static groovy.lang.Closure.DELEGATE_FIRST

class AllOfDeclaration extends WhenDeclaration {

    List<Tuple2<String,ComparatorEnum>> tags = []
    List<Tuple2<String,ComparatorEnum>> branches = []
    List<ChangeRequestDeclaration> changeRequests = []
    List<Boolean> expressions = []
    List<AnyOfDeclaration> anyOfs = []

    def tag(String pattern) {
        tags.add(new Tuple2(pattern, ComparatorEnum.GLOB))
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
        branches.add(new Tuple2(pattern, ComparatorEnum.GLOB))
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

        if (anyOfs) {
            results.addAll(anyOf(delegate))
        }

        return results.every()
    }

}
