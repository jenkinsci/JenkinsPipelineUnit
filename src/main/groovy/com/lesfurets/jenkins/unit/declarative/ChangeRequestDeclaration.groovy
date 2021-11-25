package com.lesfurets.jenkins.unit.declarative

import java.util.regex.Pattern

class ChangeRequestDeclaration extends WhenDeclaration {

    boolean all
    Tuple2<String,ComparatorEnum> id
    Tuple2<String,ComparatorEnum> target
    Tuple2<String,ComparatorEnum> branch
    Tuple2<String,ComparatorEnum> fork
    Tuple2<String,ComparatorEnum> url
    Tuple2<String,ComparatorEnum> title
    Tuple2<String,ComparatorEnum> author
    Tuple2<String,ComparatorEnum> authorDisplayName
    Tuple2<String,ComparatorEnum> authorEmail

    def ChangeRequestDeclaration (Map val) {
        if (val) {
            all = false

            ComparatorEnum comparator = null
            if (val.comparator == null) {
                comparator = ComparatorEnum.EQUALS
            }
            else {
                comparator = ComparatorEnum.getComparator(val.comparator)
            }

            id = val.id ? new Tuple2(val.id, comparator) : null
            target = val.target ? new Tuple2(val.target, comparator) : null
            branch = val.branch ? new Tuple2(val.branch, comparator) : null
            fork = val.fork ? new Tuple2(val.fork, comparator) : null
            title = val.title ? new Tuple2(val.title, comparator) : null
            url = val.url ? new Tuple2(val.url, comparator) : null
            author = val.author ? new Tuple2(val.author, comparator) : null
            authorDisplayName = val.authorDisplayName ? new Tuple2(val.authorDisplayName, comparator) : null
            authorEmail = val.authorEmail ? new Tuple2(val.authorEmail, comparator) : null
        }
        else {
            all = true
        }
    }

    Boolean execute(Object delegate) {
        def results = []

        if (all) {
            results.add(delegate?.env?.containsKey('CHANGE_ID'))
        }
        else {
            if (id) {
                results.add(compareStringToPattern(delegate.env.CHANGE_ID, id))
            }

            if (target) {
                results.add(compareStringToPattern(delegate.env.CHANGE_TARGET, target))
            }

            if (branch) {
                results.add(compareStringToPattern(delegate.env.CHANGE_BRANCH, branch))
            }

            if (fork) {
                results.add(compareStringToPattern(delegate.env.CHANGE_FORK, fork))
            }

            if (url) {
                results.add(compareStringToPattern(delegate.env.CHANGE_URL, url))
            }

            if (title) {
                results.add(compareStringToPattern(delegate.env.CHANGE_TITLE, title))
            }

            if (author) {
                results.add(compareStringToPattern(delegate.env.CHANGE_AUTHOR, author))
            }

            if (authorDisplayName) {
                results.add(compareStringToPattern(delegate.env.CHANGE_AUTHOR_DISPLAY_NAME, authorDisplayName))
            }

            if (authorEmail) {
                results.add(compareStringToPattern(delegate.env.CHANGE_AUTHOR_EMAIL, authorEmail))
            }
        }

        return results.any()
    }
}
