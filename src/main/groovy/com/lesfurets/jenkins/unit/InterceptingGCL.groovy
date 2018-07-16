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

    @Override
    Class<?> loadClass(String name) throws ClassNotFoundException {
        // Source from: groovy-all-2.4.6-sources.jar!/groovy/lang/GroovyClassLoader.java:710
        Class cls = null
        // try groovy file
        try {
            URL source = resourceLoader.loadGroovySource(name);
            // if recompilation fails, we want cls==null
            cls = recompile(source, name, null);
        } catch (IOException ioe) {
        } finally {
            if (cls == null) {
                removeClassCacheEntry(name);
            } else {
                setClassCacheEntry(cls);
            }
        }

        if (cls == null) {
            // no class found, there should have been an exception before now
            throw new AssertionError(true);
        }

        // Copy from this.parseClass(GroovyCodeSource, boolean)
        cls.metaClass.invokeMethod = helper.getMethodInterceptor()
        cls.metaClass.static.invokeMethod = helper.getMethodInterceptor()
        cls.metaClass.methodMissing = helper.getMethodMissingInterceptor()

        return cls;
    }
}