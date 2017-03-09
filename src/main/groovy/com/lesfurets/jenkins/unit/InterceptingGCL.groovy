package com.lesfurets.jenkins.unit

import org.codehaus.groovy.control.CompilationFailedException
import org.codehaus.groovy.control.CompilerConfiguration

class InterceptingGCL extends GroovyClassLoader {

    PipelineTestHelper helper

    InterceptingGCL(PipelineTestHelper helper,
                    ClassLoader loader,
                    CompilerConfiguration config) {
        super(loader, config)
        this.helper = helper
    }

    @Override
    Class parseClass(GroovyCodeSource codeSource, boolean shouldCacheSource)
                    throws CompilationFailedException {
        Class clazz = super.parseClass(codeSource, shouldCacheSource)
        clazz.metaClass.invokeMethod = helper.getMethodInterceptor()
        clazz.metaClass.static.invokeMethod = helper.getMethodInterceptor()
        clazz.metaClass.methodMissing = helper.getMethodMissingInterceptor()
        return clazz
    }
}