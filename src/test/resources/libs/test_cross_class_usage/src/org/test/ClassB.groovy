package org.test

class ClassB implements Serializable {
    ClassB() {}

    def fieldB = "I'm field of B"

    def methodB() {
        sh("echo 'ClassB: $fieldB'")
    }

}