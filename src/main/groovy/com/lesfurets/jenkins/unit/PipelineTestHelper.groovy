package com.lesfurets.jenkins.unit

import static com.lesfurets.jenkins.unit.MethodSignature.method

import java.lang.reflect.Method
import java.nio.charset.Charset
import java.nio.file.Paths
import java.util.function.Consumer
import java.util.function.Function
import java.util.regex.Pattern

import org.apache.commons.io.IOUtils
import org.codehaus.groovy.control.CompilerConfiguration
import org.codehaus.groovy.control.customizers.ImportCustomizer
import org.codehaus.groovy.runtime.InvokerHelper
import org.codehaus.groovy.runtime.MetaClassHelper

import com.lesfurets.jenkins.unit.global.lib.LibraryAnnotationTransformer
import com.lesfurets.jenkins.unit.global.lib.LibraryConfiguration
import com.lesfurets.jenkins.unit.global.lib.LibraryLoader

class PipelineTestHelper {

    protected static Method SCRIPT_SET_BINDING = Script.getMethod('setBinding', Binding.class)

    /**
     * Simple container for handling mocked scripts (`bat`, `sh`, etc).
     */
    class MockScriptHandler {
        /**
         * filter used to determine whether a script matches this mock
         *
         * The given pattern can be
         * - null, which matches anything
         * - a string, which means a full match
         * - a pattern, which means a pattern match
         */
        Object filter = null

        // mocked script results, specified as either value or callback
        String stdout = null
        int exitValue = -1
        Closure callback = null

        MockScriptHandler(Object filter, String stdout, int exitValue) {
            assert (filter == null || filter instanceof String || filter instanceof Pattern)
            this.filter = filter
            this.stdout = stdout
            this.exitValue = exitValue
        }

        MockScriptHandler(Object filter, Closure callback) {
            assert (filter == null || filter instanceof String || filter instanceof Pattern)
            this.filter = filter
            this.callback = callback
        }

        /**
         * match a script invocation against our filter
         */
        private List match(String script) {
            // if no filter is set, this matches everything
            if (filter == null) {
                return [script]
            }

            // if a string is specified, perform a simple string comparison
            if (filter instanceof String) {
                return (filter == script) ? [script] : null
            }

            // if an actual pattern is specified, perform a pattern match
            // Note that this does a full match, i.e. the call string must
            // match completely!
            if (filter instanceof Pattern) {
                def matcher = script =~ filter
                if (!matcher.matches()) {
                    return null
                }

                List results = []
                for (int i = 0; i <= matcher.groupCount(); i++) {
                    results << matcher.group(i)
                }
                return results
            }

            // should not be reached
            throw new IllegalArgumentException('Invalid filter')
        }

        /**
         * execute a mocked script
         */
        private Map execute(List matches) {
            if (callback) {
                // execute callback
                def results = callback(*matches)

                // validate the output
                if (!(results instanceof Map)) {
                    throw new IllegalArgumentException("Mocked shell callback for ${matches[0]} was not a map")
                }
                if (!results.containsKey('stdout') || !(results['stdout'] instanceof String)) {
                    throw new IllegalArgumentException("Mocked shell callback for ${matches[0]} did not contain a valid value for the stdout key")
                }
                if (!results.containsKey('exitValue') || !(results['exitValue'] instanceof Integer)) {
                    throw new IllegalArgumentException("Mocked shell callback for ${matches[0]} did not contain a valid value for the exitValue key")
                }

                return results
            } else {
                // If no callback is given, use the variables as results
                return [
                    stdout: stdout,
                    exitValue: exitValue,
                ]
            }
        }

        /**
         * handle a mocked script
         *
         * If this handler matches the script, it returns a {@code Map} with the two elements
         * <ul>
         *   <li>{@code stdout}: {@code String} with the mocked output.</li>
         *   <li>{@code exitValue}: {@code int} with the mocked exit value.</li>
         * </ul>
         * Otherwise, it returns null.
         *
         * @param script The script being executed.
         * @return the mocked script results or null when the script was not handled
         */
        Map handle(String script) {
            List matches = match(script)
            if (matches == null) {
                return null
            }
            return execute(matches)
        }
    }

    /** Holds configured mock script handlers for the `sh` command. */
    List<MockScriptHandler> mockShHandlers = [new MockScriptHandler(null, '', 0)]

    /** Holds configured mock script handlers for the `bat` command. */
    List<MockScriptHandler> mockBatHandlers = [new MockScriptHandler(null, '', 0)]

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
    Map<String, String> imports = ['Library': 'com.lesfurets.jenkins.unit.global.lib.Library']

    /**
     * Global Shared Libraries to be loaded with scripts if necessary
     * @see LibraryLoader
     */
    Map<String, LibraryConfiguration> libraries = [:]

    /**
     * Stack of method calls of scripts loaded by this helper
     */
    List<MethodCall> callStack = []

    /**
     * Controls whether method call arguments are cloned when pushed onto the
     * call stack. Disabling cloning can be useful for arguments of types that
     * are not {@code Cloneable}. Beware, however, arguments that mutate
     * during a test may appear with incorrect values in the call stack if
     * you disable cloning.
     */
    Boolean cloneArgsOnMethodCallRegistration = true

    /**
     * Internal script engine
     */
    protected GroovyScriptEngine gse

    /**
     * Loader for shared global libraries
     */
    LibraryLoader libLoader

    /** Let scripts and library classes access global vars (env, currentBuild) */
    protected Binding binding

    Map<String, Boolean> mockFileExistsResults = [:]

    Map<String, String> mockReadFileOutputs = [:]

    /**
    * Get library loader object
    *
    * @return return library loader
    * @see LibraryLoader
    */
    LibraryLoader getLibLoader() {
        return this.libLoader
    }

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
        return this.runScript(name, ((Script) delegate).binding)
    }

    /**
     * Method interceptor for method 'parallel'
     */
    protected parallelInterceptor = { Map m ->
        // If you have many steps in parallel and one of the step in Jenkins fails, the other tasks keep runnning in Jenkins.
        // Since here the parallel steps are executed sequentially, we are hiding the error to let other steps run
        // and we make the job failing at the end.
        List<String> exceptions = []
        m.entrySet().stream()
                        .filter { Map.Entry<String, Closure> entry -> entry.key != 'failFast' }
                        .forEachOrdered { Map.Entry<String, Closure> entry ->
            String parallelName = entry.key
            Closure closure = entry.value
            def result = null
            try {
                result = callClosure(closure)
            } catch (e) {
                delegate.binding.currentBuild.result = 'FAILURE'
                exceptions.add("$parallelName - ${e.getMessage()}")
            }
            return result
        }
        if (exceptions) {
            throw new RuntimeException(exceptions.join(','))
        }
    }

    /**
     * Method interceptor for any method called in executing script.
     * Calls are logged on the call stack.
     */
    public methodInterceptor = { String name, Object[] args ->
        // register method call to stack
        int depth = Thread.currentThread().stackTrace.findAll { it.className == delegate.class.name }.size()
        this.registerMethodCall(delegate, depth, name, args)
        // check if it is to be intercepted
        def intercepted = this.getAllowedMethodEntry(name, args)
        if (intercepted != null && intercepted.value) {
            intercepted.value.delegate = delegate
            return callClosure(intercepted.value, args)
        }
        // if not search for the method declaration
        MetaMethod m = delegate.metaClass.getMetaMethod(name, args)
        // ...and call it. If we cannot find it, delegate call to methodMissing
        def result = (m ? this.callMethod(m, delegate, args) : delegate.metaClass.invokeMissingMethod(delegate, name, args))
        return result
    }

    /**
     * Call given method on delegate object with args parameters
     *
     * @param method method to call
     * @param delegate object of the method call
     * @param args method call parameters
     * @return return value of the object
     */
    protected Object callMethod(MetaMethod method, Object delegate, Object[] args) {
        return method.doMethodInvoke(delegate, args)
    }

    def getMethodInterceptor() {
        return methodInterceptor
    }

    /**
     * Method for calling custom allowed methods
     */
    def methodMissingInterceptor = { String name, args ->
        if (this.isMethodAllowed(name, args)) {
            def result = null
            if (args != null) {
                for (argument in args) {
                    result = this.callIfClosure(argument, result)
                    if (argument instanceof Map) {
                        argument.each { k, v ->
                            result = this.callIfClosure(k, result)
                            result = this.callIfClosure(v, result)
                        }
                    }
                }
            }
            return result
        } else {
            throw new MissingMethodException(name, delegate.class, args)
        }
    }

    def getMethodMissingInterceptor() {
        return methodMissingInterceptor
    }

    def callIfClosure(Object closure, Object currentResult) {
        if (closure instanceof Closure) {
            currentResult = callClosure(closure)
        }
        return currentResult
    }

    /**
     * Method interceptor for 'libraryResource' in Shared libraries
     * The resource from shared library should have been added to the url classloader in advance
     */
    def libraryResourceInterceptor = { m ->
        def stream = gse.groovyClassLoader.getResourceAsStream(m as String)
        if (stream) {
            def string = IOUtils.toString(stream, Charset.forName("UTF-8"))
            IOUtils.closeQuietly(stream)
            return string
        } else {
            throw new GroovyRuntimeException("Library Resource not found with path $m")
        }
    }

    /**
     * List of allowed methods with default interceptors.
     * Complete this list in need with {@link #registerAllowedMethod}
     */
    protected Map<MethodSignature, Closure> allowedMethodCallbacks = [
                    (method("load", String.class))           : loadInterceptor,
                    (method("parallel", Map.class))          : parallelInterceptor,
                    (method("libraryResource", String.class)): libraryResourceInterceptor,
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

    PipelineTestHelper init() {
        CompilerConfiguration configuration = new CompilerConfiguration()
        GroovyClassLoader cLoader = new InterceptingGCL(this, baseClassloader, configuration, binding)

        libLoader = new LibraryLoader(cLoader, libraries)
        LibraryAnnotationTransformer libraryTransformer = new LibraryAnnotationTransformer(libLoader)
        configuration.addCompilationCustomizers(libraryTransformer)

        ImportCustomizer importCustomizer = new ImportCustomizer()
        imports.each { k, v -> importCustomizer.addImport(k, v) }
        configuration.addCompilationCustomizers(importCustomizer)

        configuration.setDefaultScriptExtension(scriptExtension)
        configuration.setScriptBaseClass(scriptBaseClass.getName())

        gse = new GroovyScriptEngine(scriptRoots, cLoader)
        gse.setConfig(configuration)

        mockShHandlers = [new MockScriptHandler(null, '', 0)]
        mockBatHandlers = [new MockScriptHandler(null, '', 0)]
        mockFileExistsResults.clear()
        mockReadFileOutputs.clear()
        return this
    }

    /**
     *
     * @return true if internal GroovyScriptEngine is set
     */
    boolean isInitialized() {
        return gse != null
    }

    /**
     * Clone argments for registerMethodCall so they are recorded
     * at the time the method is called in case of changes to an
     * arg variable later in the pipeline.
     *
     * @param args original args passed to the method call
     * @return cloned args
     */
    private Object[] cloneArgs(Object[] args) {

        List argsCloned = []
        args.each {
            try {
                // Try the clone
                argsCloned << it?.clone()
            }
            catch(e) {
                // Cannot clone it, get a string representation at this point.
                argsCloned << it.toString()
            }
        }
        return argsCloned as Object[]
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
        if (cloneArgsOnMethodCallRegistration) {
            call.args = cloneArgs(args)
        } else {
            call.args = args
        }
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
    protected Map.Entry<MethodSignature, Closure> getAllowedMethodEntry(String name, Object... args) {
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
     * Load the script with name and empty binding, returning the Script
     * @param name path of the script
     * @return Script object
     */
    Script loadScript(String name) {
        return this.loadScript(name, new Binding())
    }

    /**
     * Load the script with given binding context without running, returning the Script
     * @param scriptName
     * @param binding
     * @return Script object
     */
    Script loadScript(String scriptName, Binding binding) {
        Objects.requireNonNull(binding, "Binding cannot be null.")
        Objects.requireNonNull(gse, "GroovyScriptEngine is not initialized: Initialize the helper by calling init().")
        Class scriptClass = gse.loadScriptByName(scriptName)
        setGlobalVars(binding)
        Script script = InvokerHelper.createScript(scriptClass, binding)
        InterceptingGCL.interceptClassMethods(script.metaClass, this, binding)
        return script
    }

    /**
     * Load the script code with empty binding, returning the Script
     * @param scriptText code for the script
     * @return Script object
     */
    Script loadInlineScript(String scriptText) {
        return this.loadInlineScript(scriptText, new Binding())
    }

    /**
     * Load the script code with given binding context without running, return the Script
     * @param scriptText
     * @param binding
     * @return Script object
     */
    Script loadInlineScript(String scriptText, Binding binding) {
        Objects.requireNonNull(binding, "Binding cannot be null.")
        Objects.requireNonNull(gse, "GroovyScriptEngine is not initialized: Initialize the helper by calling init().")
        GroovyShell shell = new GroovyShell(gse.getParentClassLoader(), binding, gse.getConfig())
        Script script = shell.parse(scriptText)
        // make sure to set global vars after parsing the script as it will trigger library loads, otherwise library methods will be unregistered
        setGlobalVars(binding)
        InterceptingGCL.interceptClassMethods(script.metaClass, this, binding)
        return script
    }

    /**
     * Load and run the script, returning the result value;
     * @param scriptName
     * @param binding
     * @return the return value of the script
     */
    Object runScript(String scriptName, Binding binding) {
        return runScriptInternal(loadScript(scriptName, binding))
    }

    /**
     * Load and run the script, returning the result value;
     * @param scriptName
     * @return the return value of the script
     */
    Object runScript(String scriptName) {
        return runScriptInternal(loadScript(scriptName, new Binding()))
    }

    /**
     * Load and run the script, returning the result value;
     * @param scriptText
     * @param binding
     * @return the return value of the script
     */
    Script runInlineScript(String scriptText, Binding binding) {
        return runScriptInternal(loadInlineScript(scriptText, binding))
    }

    /**
     * Load and run the script, returning the result value;
     * @param scriptText
     * @return the return value of the script
     */
    Script runInlineScript(String scriptText) {
        return runScriptInternal(loadInlineScript(scriptText, new Binding()))
    }

    /**
     * Run the given script object
     * @param Script object
     * @return the return value of the script
     */
    Object runScript(Script script) {
        return this.runScriptInternal(script)
    }

    /**
     * Run the script
     * @param script
     * @return the return value of the script
     */
    protected Object runScriptInternal(Script script) {
        return script.run()
    }

    /**
     * Sets global variables defined in loaded libraries on the binding
     * @param binding
     */
    public void setGlobalVars(Binding binding) {
        libLoader.libRecords.values().stream()
                        .flatMap { it.definedGlobalVars.entrySet().stream() }
                        .forEach { e ->
            if (e.value instanceof Script) {
                Script script = Script.cast(e.value)
                // invoke setBinding from method to avoid interception
                SCRIPT_SET_BINDING.invoke(script, binding)
                script.metaClass.getMethods().findAll { it.name == 'call' }.forEach { m ->
                    this.registerAllowedMethod(method(e.value.class.name, m.getNativeParameterTypes()),
                        { args ->
                            // When calling a one argument method with a null argument the
                            // Groovy doMethodInvoke appears to incorrectly assume a zero
                            // argument call signature for the method yielding an IllegalArgumentException
                            if (args == null && m.getNativeParameterTypes().size() == 1) {
                                m.doMethodInvoke(e.value, MetaClassHelper.ARRAY_WITH_NULL)
                            } else {
                                m.doMethodInvoke(e.value, args)
                            }
                        })
                }
            }
            binding.setVariable(e.key, e.value)
        }
    }

    /**
     * @param name method name
     * @param closure method implementation, can be null
     */
    void registerAllowedMethod(String name, Closure closure = null) {
        allowedMethodCallbacks.put(method(name), closure)
    }

    /**
     * @param name method name
     * @param args parameter types
     * @param closure method implementation, can be null
     */
    void registerAllowedMethod(String name, List<Class> args, Closure closure = null) {
        allowedMethodCallbacks.put(method(name, args.toArray(new Class[args?.size()])), closure)
    }

    /**
     * Register a callback implementation for a method
     * Calls from the loaded scripts to allowed methods will call the given implementation
     * Null callbacks will only log the call and do nothing
     * @param methodSignature method signature
     * @param closure method implementation, can be null
     */
    void registerAllowedMethod(MethodSignature methodSignature, Closure closure = null) {
        allowedMethodCallbacks.put(methodSignature, closure)
    }

    /**
     *
     * @param methodSignature
     * @param callback
     */
    void registerAllowedMethod(MethodSignature methodSignature, Function callback) {
        this.registerAllowedMethod(methodSignature,
                        callback != null ? { params -> return callback.apply(params) } : null)
    }

    /**
     *
     * @param methodSignature
     * @param callback
     */
    void registerAllowedMethod(MethodSignature methodSignature, Consumer callback) {
        this.registerAllowedMethod(methodSignature,
                        callback != null ? { params -> return callback.accept(params) } : null)
    }

    /**
     * Register library description
     * See {@link LibraryConfiguration} for its description
     * @param libraryDescription to add
     */
    void registerSharedLibrary(LibraryConfiguration libraryDescription) {
        Objects.requireNonNull(libraryDescription)
        Objects.requireNonNull(libraryDescription.name)
        this.libraries.put(libraryDescription.name, libraryDescription)
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

    /**
     * Verifies if a method was called, with the preconditions defined in times and methodVerification, if wanted.
     * @param name the method name
     * @param times times the method shall be called.
     * @param methodVerification a closure with the a MethodSignature object as input parameter, which verifies a condition
     */
    void verify(String name, int times = 1, Closure methodVerification = { return true }) {
        List<MethodCall> methodCalls = callStack.findAll { it.getMethodName() == name }
        methodCalls.each { call ->
            if (!methodVerification(call)) {
                throw new VerificationException("Method call $call failed to be verified")
            }
        }
        int timesCalled = methodCalls.size()
        if (times != timesCalled) {
            throw new VerificationException("Expected method $name to be called $times times, but was called $timesCalled times")
        }
    }

    /**
     * Call closure by handling spreading of parameter default values
     *
     * @param closure to call
     * @param args array of arguments passed to this closure call. Is null by default.
     * @return result of the closure call
     */
    Object callClosure(Closure closure, Object[] args = null) {
        // When we use a library method, we should not spread the argument because we define a closure with a single
        // argument. The arguments will be spread in this closure (See PipelineTestHelper#setGlobalVars)
        // For other cases, we spread it before calling
        // Note : InvokerHelper.invokeClosure(intercepted.value, args) is similar to closure.call(*args)
        if (!args) {
            return closure.call()
        } else if (args.size() > closure.maximumNumberOfParameters) {
            return closure.call(args)
        } else {
            return closure.call(*args)
        }
    }

    void addFileExistsMock(String file, Boolean result) {
        mockFileExistsResults[file] = result
    }

    Boolean fileExists(String arg) {
        return mockFileExistsResults[arg] ?: false
    }

    void addReadFileMock(String file, String contents) {
        mockReadFileOutputs[file] = contents
    }

    String readFile(def args) {
        String file = null
        if (args instanceof String || args instanceof GString) {
            file = args
        } else if (args instanceof Map) {
            file = args['file']
        }
        assert file

        return mockReadFileOutputs[file] ?: ''
    }

    /**
     * Configure mock output for the `sh` command. This function should be called before
     * attempting to call `JenkinsMocks.sh()`.
     * @param filter Script command or pattern to mock or null if any command is matched.
     * @param stdout Standard output text to return for the given command.
     * @param exitValue Exit value for the command.
     */
    void addShMock(Object filter, String stdout, int exitValue) {
        mockShHandlers << new MockScriptHandler(filter, stdout, exitValue)
    }

    /**
     * Configure mock callback for the `sh` command. This function should be called before
     * attempting to call `JenkinsMocks.sh()`.
     * @param filter Script command or pattern to mock or null if any command is matched.
     * @param callback Closure to be called when the mock is executed. This closure will be
     *                 passed the script call which is being executed, and
     *                 <strong>must</strong> return a {@code Map} with the following
     *                 key/value pairs:
     *                 <ul>
     *                   <li>{@code stdout}: {@code String} with the mocked output.</li>
     *                   <li>{@code exitValue}: {@code int} with the mocked exit value.</li>
     *                 </ul>
     */
    void addShMock(Object filter, Closure callback) {
        mockShHandlers << new MockScriptHandler(filter, callback)
    }

    @SuppressWarnings('ThrowException')
    def runSh(def args) {
        return runScript(args, mockShHandlers)
    }

    /**
     * Configure mock output for the `bat` command. This function should be called before
     * attempting to call `JenkinsMocks.bat()`.
     * @param filter Script command or pattern to mock or null if any command is matched.
     * @param stdout Standard output text to return for the given command.
     * @param exitValue Exit value for the command.
     */
    void addBatMock(Object filter, String stdout, int exitValue) {
        mockBatHandlers << new MockScriptHandler(filter, stdout, exitValue)
    }

    /**
     * Configure mock callback for the `bat` command. This function should be called before
     * attempting to call `JenkinsMocks.bat()`.
     * @param filter Script command or pattern to mock or null if any command is matched.
     * @param callback Closure to be called when the mock is executed. This closure will be
     *                 passed the script call which is being executed, and
     *                 <strong>must</strong> return a {@code Map} with the following
     *                 key/value pairs:
     *                 <ul>
     *                   <li>{@code stdout}: {@code String} with the mocked output.</li>
     *                   <li>{@code exitValue}: {@code int} with the mocked exit value.</li>
     *                 </ul>
     */
    void addBatMock(Object filter, Closure callback) {
        mockBatHandlers << new MockScriptHandler(filter, callback)
    }

    @SuppressWarnings('ThrowException')
    def runBat(def args) {
        return runScript(args, mockBatHandlers)
    }

    @SuppressWarnings('ThrowException')
    def runScript(def args, List<MockScriptHandler> mockScriptHandlers) {
        String script = null
        boolean returnStdout = false
        boolean returnStatus = false

        // The shell functions can be called with either a string, or a map of key/value pairs.
        if (args instanceof String || args instanceof GString) {
            script = args
        } else if (args instanceof Map) {
            script = args['script']
            returnStatus = args['returnStatus'] ?: false
            returnStdout = args['returnStdout'] ?: false
            if (returnStatus && returnStdout) {
                throw new IllegalArgumentException('returnStatus and returnStdout are mutually exclusive options')
            }
        }
        assert script

        // find the last handler added matching the script
        Map results
        mockScriptHandlers.reverse().find { handler ->
            results = handler.handle(script)
            return results != null
        }
        assert results

        // Jenkins also prints the output from the shell when returnStdout is true if the script fails
        if (!returnStdout || results.exitValue != 0) {
            println results.stdout
        }

        if (returnStdout && results.exitValue == 0) {
            return results.stdout
        }
        if (returnStatus) {
            return results.exitValue
        }
        if (results.exitValue != 0) {
            throw new Exception('script returned exit code ' + results.exitValue)
        }
        return null
    }

}
