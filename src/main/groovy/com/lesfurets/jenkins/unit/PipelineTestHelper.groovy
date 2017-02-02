package com.lesfurets.jenkins.unit

import static com.lesfurets.jenkins.unit.MethodSignature.method

import java.nio.file.Paths
import java.util.function.Consumer
import java.util.function.Function

import org.codehaus.groovy.control.CompilerConfiguration
import org.codehaus.groovy.control.customizers.ImportCustomizer
import org.codehaus.groovy.runtime.MetaClassHelper


class PipelineTestHelper {

    /**
     * Search paths for scripts
     */
    String[] scriptRoots

    /**
     * Base path for script roots.
     * Usually the path to the project.
     */
    String baseScriptRoot

    /**
     * Extension for script files.
     * Ex. jenkins
     */
    String scriptExtension

    /**
     * Base class for instantiated scripts
     */
    Class scriptBaseClass = MockPipelineScript.class

    /**
     * Classloader to instantiate scripts
     */
    ClassLoader baseClassloader

    /**
     * Default imports for scripts loaded by this helper
     */
    Map<String, String> imports

    /**
     * Stack of method calls of scripts loaded by this helper
     */
    List<MethodCall> callStack = []

    private GroovyScriptEngine gse

    /**
     * Method interceptor for method 'load' to load scripts via encapsulated GroovyScriptEngine
     */
    protected loadInterceptor = { args ->
        String name = args
        // The script is loaded by its normal name :
        def relativize = Paths.get(baseScriptRoot).relativize(Paths.get(name)).normalize()
        if (relativize.toFile().exists()) {
            name = relativize.toString()
        } else {
            // The script is loaded from its full name :
            scriptRoots.eachWithIndex { it, i ->
                def resolved = Paths.get(baseScriptRoot, it).resolve(name).normalize()
                if (resolved.toFile().exists()) {
                    name = resolved.toString()
                }
            }
        }
        return this.loadScript(name, delegate.binding)
    }

    protected parallelInterceptor = { Map m ->
        // If you have many steps in parallel and one of the step in Jenkins fails, the other tasks keep runnning in Jenkins.
        // Since here the parallel steps are executed sequentially, we are hiding the error to let other steps run
        // and we make the job failing at the end.
        List<String> exceptions = []
        m.forEach { String parallelName, Closure closure ->
            try {
                return closure.call()
            } catch (e) {
                delegate.binding.currentBuild.result = 'FAILURE'
                exceptions.add("$parallelName - ${e.getMessage()}")
            }
        }
        if (exceptions) {
            throw new Exception(exceptions.join(','))
        }
    }

    /**
     * Method interceptor for any method called in executing script.
     * Calls are logged on the call stack.
     */
    protected methodInterceptor = { String name, args ->
        // register method call to stack
        int depth = Thread.currentThread().stackTrace.findAll { it.className == delegate.class.name }.size()
        this.registerMethodCall(delegate, depth, name, args)
        // check if it is to be intercepted
        def intercepted = this.getAllowedMethodEntry(name, args)
        if (intercepted != null && intercepted.value) {
            intercepted.value.delegate = delegate
            return intercepted.value.call(*args)
        }
        // if not search for the method declaration
        MetaMethod m = delegate.metaClass.getMetaMethod(name, *args)

        // Fix for GString - String incompatibility in method invocation
        def argsWithoutGstring = args?.collect { it instanceof GString ? it as String : it }?.toArray()
        // ...and call it. If we cannot find it, delegate call to methodMissing
        def result = (m ? m.invoke(delegate, *argsWithoutGstring) : delegate.metaClass.invokeMissingMethod(delegate, name, args))
        return result
    }

    /**
     * List of allowed methods with default interceptors.
     * Complete this list in need with {@link #registerAllowedMethod}
     */
    protected Map<MethodSignature, Closure> allowedMethodCallbacks = [
            (method("load", String.class))                : loadInterceptor,
            (method("parallel", Map.class))               : parallelInterceptor,
    ]

    PipelineTestHelper() {
    }

    PipelineTestHelper(String[] scriptRoots,
                       String scriptExtension,
                       Class scriptBaseClass,
                       Map<String, String> imports,
                       ClassLoader baseClassloader, String baseScriptRoot) {
        this.scriptRoots = scriptRoots
        this.scriptExtension = scriptExtension
        this.scriptBaseClass = scriptBaseClass
        this.imports = imports
        this.baseClassloader = baseClassloader
        this.baseScriptRoot = baseScriptRoot
    }

    PipelineTestHelper build() {
        ImportCustomizer customizer = new ImportCustomizer()
        imports.each { k, v -> customizer.addImport(k, v) }

        CompilerConfiguration configuration = new CompilerConfiguration()
        configuration.setDefaultScriptExtension(scriptExtension)
        configuration.setScriptBaseClass(scriptBaseClass.getName())
        configuration.addCompilationCustomizers(customizer)

        GroovyClassLoader cLoader = new GroovyClassLoader(baseClassloader, configuration)
        gse = new GroovyScriptEngine(scriptRoots, cLoader)

        gse.setConfig(configuration)
        return this
    }

    /**
     * Register method call to call stack
     * @param target target object
     * @param stackDepth depth in stack
     * @param name method name
     * @param args method arguments
     */
    protected void registerMethodCall(Object target, int stackDepth, String name, Object... args) {
        MethodCall call = new MethodCall()
        call.target = target
        call.methodName = name
        call.args = args
        call.stackDepth = stackDepth
        callStack.add(call)
    }

    /**
     * Search for the allowed method entry <MethodSignature, Closure>
     *     A null Closure will mean that the method is allowed but not intercepted.
     * @param name method name
     * @param args parameter objects
     * @return Map.Entry corresponding to the method <MethodSignature, Closure>
     */
    protected Map.Entry<MethodSignature, Closure> getAllowedMethodEntry(String name, args) {
        Class[] paramTypes = MetaClassHelper.castArgumentsToClassArray(args)
        MethodSignature signature = method(name, paramTypes)
        return allowedMethodCallbacks.find { k, v -> k == signature }
    }

    /**
     *
     * @param name method name
     * @param args parameter objects
     * @return true if method is allowed in this helper
     */
    protected boolean isMethodAllowed(String name, args) {
        return getAllowedMethodEntry(name, args) != null
    }

    /**
     * Load and run script with given binding context
     * @param scriptName path of the script
     * @param binding
     * @return loaded and run script
     */
    Script loadScript(String scriptName, Binding binding) {
        Objects.requireNonNull(binding)
        binding.setVariable("_TEST_HELPER", this)
        Script script = gse.createScript(scriptName, binding)
        script.metaClass.invokeMethod = methodInterceptor
        script.metaClass.static.invokeMethod = methodInterceptor
        script.run()
        return script
    }

    /**
     * Load script with name with empty binding
     * @param name path of the script
     * @return loaded and run script
     */
    Script loadScript(String name) {
        this.loadScript(name, new Binding())
    }

    /**
     * @param name method name
     * @param args parameter types
     * @param closure method implementation, can be null
     */
    void registerAllowedMethod(String name, List<Class> args, Closure closure) {
        allowedMethodCallbacks.put(method(name, args.toArray(new Class[args.size()])), closure)
    }

    /**
     * Register a callback implementation for a method
     * Calls from the loaded scripts to allowed methods will call the given implementation
     * Null callbacks will only log the call and do nothing
     * @param methodSignature method signature
     * @param closure method implementation, can be null
     */
    void registerAllowedMethod(MethodSignature methodSignature, Closure closure) {
        allowedMethodCallbacks.put(methodSignature, closure)
    }

    /**
     *
     * @param methodSignature
     * @param callback
     */
    void registerAllowedMethod(MethodSignature methodSignature, Function callback) {
        this.registerAllowedMethod(methodSignature,
                        callback != null ? { params -> return callback.apply(params)} : null)
    }

    /**
     *
     * @param methodSignature
     * @param callback
     */
    void registerAllowedMethod(MethodSignature methodSignature, Consumer callback) {
        this.registerAllowedMethod(methodSignature,
                        callback != null ? { params -> return callback.accept(params)} : null)
    }

    /**
     * Clear call stack
     */
    void clearCallStack() {
        callStack.clear()
    }

    /**
     * Count the number of calls to the method with name
     * @param name method name
     * @return call number
     */
    long methodCallCount(String name) {
        callStack.stream().filter { call ->
            call.methodName == name
        }.count()
    }

}
