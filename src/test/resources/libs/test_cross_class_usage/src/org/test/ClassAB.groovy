package org.test

class ClassAB implements Serializable {

    def methodAB() {
        new ClassA().methodA()
        new ClassB().methodB()
    }

}