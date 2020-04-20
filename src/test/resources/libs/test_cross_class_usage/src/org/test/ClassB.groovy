package org.test

class ClassB implements Serializable {
    def script = null
    ClassB() {}
    ClassB(script) {
        this.script = script
    }

    def fieldB = "I'm field of B"

    def methodB() {
        if(script) {
            script.sh("echo 'ClassB: $fieldB'")
        } else {
            sh("echo 'ClassB: $fieldB'")
        }
    }

}