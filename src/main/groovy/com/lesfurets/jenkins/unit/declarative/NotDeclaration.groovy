package com.lesfurets.jenkins.unit.declarative

class NotDeclaration extends WhenDeclaration {

    Boolean execute(Object delegate) {
        return !super.execute(delegate)
    }
}

