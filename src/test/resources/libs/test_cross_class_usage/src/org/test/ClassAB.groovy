package org.test

class ClassAB implements Serializable {
    def script = null
    ClassAB() {}
    ClassAB(script) {
        this.script = script
    }

    def methodAB() {
        if (script) {
            new ClassA(script).methodA()
            new ClassB(script).methodB()

        } else {
            new ClassA().methodA()
            new ClassB().methodB()
        }
    }

    def fieldAB = "I'm field of AB"

    def ownMethod() {
        if(script) {
            script.sh("echo 'ClassAB: $fieldAB'")
        } else {
            sh("echo 'ClassAB: $fieldAB'")
        }
    }

}