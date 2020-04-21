package com.lesfurets.jenkins.unit

import org.codehaus.groovy.control.CompilationFailedException
import org.codehaus.groovy.control.CompilerConfiguration

import static com.lesfurets.jenkins.unit.MethodSignature.method

class InterceptingGCL extends GroovyClassLoader {

    static void interceptClassMethods(MetaClass metaClazz, PipelineTestHelper helper) {
        metaClazz.invokeMethod = helper.getMethodInterceptor()
        metaClazz.static.invokeMethod = helper.getMethodInterceptor()
        metaClazz.methodMissing = helper.getMethodMissingInterceptor()

        // find and replace script method closure with any matching allowed method closure
        metaClazz.methods.forEach { scriptMethod ->
            def signature = method(scriptMethod.name, scriptMethod.nativeParameterTypes)
            Map.Entry<MethodSignature, Closure> matchingMethod = helper.allowedMethodCallbacks.find { k, v -> k == signature }
            if (matchingMethod) {
                // a matching method was registered, replace script method execution call with the registered closure (mock)
                metaClazz."$scriptMethod.name" = matchingMethod.value
            }
        }
    }

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
        println "Parsing class: ${codeSource.getName()}"
        Class clazz = super.parseClass(codeSource, shouldCacheSource)
        interceptClassMethods(clazz.metaClass, helper)
        return clazz
    }

    @Override
    Class<?> loadClass(String name) throws ClassNotFoundException {
        // Source from: groovy-all-2.4.6-sources.jar!/groovy/lang/GroovyClassLoader.java:710
        println "Loading class: $name"
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
            // no class found, using parent's method
            cls = super.loadClass(name)
        }

        // Copy from this.parseClass(GroovyCodeSource, boolean)
        cls.metaClass.invokeMethod = helper.getMethodInterceptor()
        cls.metaClass.static.invokeMethod = helper.getMethodInterceptor()
        cls.metaClass.methodMissing = helper.getMethodMissingInterceptor()

        return cls;
    }
}