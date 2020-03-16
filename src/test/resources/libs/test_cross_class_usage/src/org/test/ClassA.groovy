package org.test

class ClassA implements Serializable {
    ClassA() {}

    def fieldA = "I'm field of A"

    def methodA() {
        sh("echo 'ClassA: $fieldA'")
    }
}