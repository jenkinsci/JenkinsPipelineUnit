package com.lesfurets.jenkins.unit.declarative

class NotDeclaration extends WhenDeclaration {

    boolean execute(Object delegate) {
        return !super.execute(delegate)
    }
}

