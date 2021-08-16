package com.lesfurets.jenkins.unit.declarative

import java.util.regex.Pattern

class ChangeRequestDeclaration extends WhenDeclaration {

    boolean all
    String id
    String target
    String branch
    String fork
    String url
    String title
    String author
    String authorDisplayName
    String authorEmail
    String comparator

    def ChangeRequestDeclaration (Map val) {
        if (val) {
            this.all = false
            this.id = val.id
            this.target = val.target
            this.branch = val.branch
            this.fork = val.fork
            this.title = val.title
            this.url = val.url
            this.author = val.author
            this.authorDisplayName = val.authorDisplayName
            this.authorEmail = val.authorEmail
            this.comparator = val.comparator
        }
        else {
            this.all = true
        }
    }

    Boolean compare(String expected, String actual) {
        Boolean result
        if (!this.comparator || this.comparator == "EQUALS") {
            result = actual == expected
        }
        else if (this.comparator == "GLOB") {
            Pattern expectedPattern = getPatternFromGlob(expected)
            result = actual ==~ expectedPattern.pattern()
        }
        else if (this.comparator == "REGEXP") {
            result = actual ==~ expected
        }
        else {
            throw new IllegalArgumentException("Invalid comparator for changeRequest '${this.comparator}'")
        }
        return result
    }

    Boolean execute(Object delegate) {
        def results = []

        if (this.all) {
            results.add(delegate?.env?.containsKey("CHANGE_ID"))
        }
        else {
            if (this.id) {
                results.add(compare(this.id, delegate.env.CHANGE_ID))
            }

            if (this.target) {
                results.add(compare(this.target, delegate.env.CHANGE_TARGET))
            }

            if (this.branch) {
                results.add(compare(this.branch, delegate.env.CHANGE_BRANCH))
            }

            if (this.fork) {
                results.add(compare(this.fork, delegate.env.CHANGE_FORK))
            }

            if (this.url) {
                results.add(compare(this.url, delegate.env.CHANGE_URL))
            }

            if (this.title) {
                results.add(compare(this.title, delegate.env.CHANGE_TITLE))
            }

            if (this.author) {
                results.add(compare(this.author, delegate.env.CHANGE_AUTHOR))
            }

            if (this.authorDisplayName) {
                results.add(compare(this.authorDisplayName, delegate.env.CHANGE_AUTHOR_DISPLAY_NAME))
            }

            if (this.authorEmail) {
                results.add(compare(this.authorEmail, delegate.env.CHANGE_AUTHOR_EMAIL))
            }
        }

        return results.any()
    }
}
