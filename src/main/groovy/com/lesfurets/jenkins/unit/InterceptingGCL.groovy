package com.lesfurets.jenkins.unit

import org.codehaus.groovy.control.CompilationFailedException
import org.codehaus.groovy.control.CompilerConfiguration

import static com.lesfurets.jenkins.unit.MethodSignature.method

class InterceptingGCL extends GroovyClassLoader {

    static void interceptClassMethods(MetaClass metaClazz, PipelineTestHelper helper, Binding binding) {
        metaClazz.invokeMethod = helper.getMethodInterceptor()
        metaClazz.static.invokeMethod = helper.getMethodInterceptor()
        metaClazz.methodMissing = helper.getMethodMissingInterceptor()
        metaClazz.getEnv = {return binding.env}
        // find and replace script method closure with any matching allowed method closure
        metaClazz.methods.forEach { scriptMethod ->
            def signature = method(scriptMethod.name, scriptMethod.nativeParameterTypes)
            Map.Entry<MethodSignature, Closure> matchingMethod = helper.allowedMethodCallbacks.find { k, v -> k == signature }
            if (matchingMethod) {
                // a matching method was registered, replace script method execution call with the registered closure (mock)
                metaClazz."$scriptMethod.name" = matchingMethod.value ?: defaultClosure(matchingMethod.key.args)
            }
        }
    }

    static Closure defaultClosure(Class[] args) {
        int maxLength = 254
        if (args.length > maxLength) { throw new IllegalArgumentException("Only $maxLength arguments allowed")}
        String argumentsString = args.inject("") { acc, value ->
            return "${acc}${value.name} ${'a'*(acc.count(',') + 1)},"}
        String argumentsStringWithoutComma = argumentsString.size() > 0 ?
                argumentsString.substring(0, argumentsString.length()-1) : argumentsString
        String closureString = "{$argumentsStringWithoutComma -> }"
        return (Closure)new GroovyShell().evaluate(closureString)
    }

    PipelineTestHelper helper
    Binding binding

    InterceptingGCL(PipelineTestHelper helper,
                    ClassLoader loader,
                    CompilerConfiguration config,
                    Binding binding) {
        super(loader, config)
        this.helper = helper
        this.binding = binding
    }

    @Override
    Class parseClass(GroovyCodeSource codeSource, boolean shouldCacheSource)
            throws CompilationFailedException {
        Class clazz = super.parseClass(codeSource, shouldCacheSource)
        interceptClassMethods(clazz.metaClass, helper, binding)
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
            // no class found, using parent's method
            return super.loadClass(name)
        }

        // Copy from this.parseClass(GroovyCodeSource, boolean)
        cls.metaClass.invokeMethod = helper.getMethodInterceptor()
        cls.metaClass.static.invokeMethod = helper.getMethodInterceptor()
        cls.metaClass.methodMissing = helper.getMethodMissingInterceptor()

        return cls;
    }
}