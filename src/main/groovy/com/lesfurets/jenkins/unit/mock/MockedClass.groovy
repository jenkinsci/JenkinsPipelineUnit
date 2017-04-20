package com.lesfurets.jenkins.unit.mock

import com.lesfurets.jenkins.unit.MethodSignature

class MockedClass {

    public final static Closure<Object> DEFAULT_INITIALIZER = { it -> return it }

    final String fullQualifiedName
    final String packageName
    final String className
    final Closure<Object> initializer
    Map<MethodSignature, Closure> mockedMethods = [:]

    static MockedClass mockedClass(String fullQualifiedName, Closure<Object> initializer = null) {
        return new MockedClass(fullQualifiedName, initializer)
    }

    MockedClass(String fullQualifiedName, Closure<Object> initializer = null) {
        this.fullQualifiedName = fullQualifiedName
        this.initializer = initializer ?: DEFAULT_INITIALIZER
        def lastDot = fullQualifiedName.lastIndexOf('.')
        this.packageName = fullQualifiedName.substring(0, lastDot)
        this.className = fullQualifiedName.substring(lastDot + 1, fullQualifiedName.size())
    }

    MockedClass mock(MethodSignature signature, Closure methodBody) {
        mockedMethods.put(signature, methodBody)
        return this
    }

    String getPackageName() {
        return this.packageName
    }

    String getClassName() {
        return this.className
    }

    Object initialize(Object toInitialize, Object[] arguments) {
        return this.initializer.call(toInitialize, arguments)
    }

}
