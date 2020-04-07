package com.lesfurets.jenkins.unit

import static java.util.stream.Collectors.joining
import static org.assertj.core.api.Assertions.assertThat

import org.assertj.core.api.AbstractCharSequenceAssert

import groovy.transform.Memoized

abstract class BasePipelineTest {

    PipelineTestHelper helper

    String[] scriptRoots = ["src/main/jenkins", "./."]

    String scriptExtension = "jenkins"

    Map<String, String> imports = ["NonCPS": "com.cloudbees.groovy.cps.NonCPS"]

    String baseScriptRoot = "."

    Binding binding = new Binding()

    ClassLoader baseClassLoader = this.class.classLoader

    def stringInterceptor = { m -> m.variable }

    def withCredentialsInterceptor = { list, closure ->
        list.forEach {
            binding.setVariable(it, "$it")
        }
        def res = closure.call()
        list.forEach {
            binding.setVariable(it, null)
        }
        return res
    }

    BasePipelineTest(PipelineTestHelper helper) {
        this.helper = helper
    }

    BasePipelineTest() {
        this(new PipelineTestHelper())
    }

    void setUp() throws Exception {
        if (!helper.isInitialized()) {
            initHelper()
        } else {
            helper.callStack.clear()
        }

        registerAllowedMethods()
        setVariables()
    }

    PipelineTestHelper initHelper() {
        helper.with {
            it.scriptRoots = this.scriptRoots
            it.scriptExtension = this.scriptExtension
            it.baseClassloader = this.baseClassLoader
            it.imports += this.imports
            it.baseScriptRoot = this.baseScriptRoot
            return it
        }.init()
    }

    void registerAllowedMethods() {
        // Please keep this list sorted when adding new mocks!
        helper.registerAllowedMethod("addHtmlBadge", [Map])
        helper.registerAllowedMethod("addShortText", [Map])
        helper.registerAllowedMethod('archive', [Map])
        helper.registerAllowedMethod('archive', [String])
        helper.registerAllowedMethod("archiveArtifacts", [Map])
        helper.registerAllowedMethod('archiveArtifacts', [String])
        helper.registerAllowedMethod("bat", [String])
        helper.registerAllowedMethod("build", [Map], {
            [
                getNumber:{100500},
                getDescription:{"Dummy build description"}
            ]
        })
        helper.registerAllowedMethod("buildDiscarder", [Object])
        helper.registerAllowedMethod("checkout", [Map])
        helper.registerAllowedMethod("choice", [Map])
        helper.registerAllowedMethod('cifsPublisher', [Map], {true})
        helper.registerAllowedMethod('cleanWs')
        helper.registerAllowedMethod('copyArtifacts', [Map], {true})
        helper.registerAllowedMethod("cron", [String])
        helper.registerAllowedMethod('deleteDir')
        helper.registerAllowedMethod("dir", [String, Closure]) { String path, Closure c ->
            c.delegate = delegate
            helper.callClosure(c)
        }
        helper.registerAllowedMethod("disableConcurrentBuilds")
        helper.registerAllowedMethod("echo", [String]) { String message ->
            println(message)
        }
        helper.registerAllowedMethod("error", [String], { updateBuildStatus('FAILURE') })
        helper.registerAllowedMethod("gatlingArchive")
        helper.registerAllowedMethod("gitlabBuilds", [Map, Closure])
        helper.registerAllowedMethod("gitlabCommitStatus", [String, Closure], { String name, Closure c ->
            c.delegate = delegate
            helper.callClosure(c)
        })
        helper.registerAllowedMethod("input", [String])
        helper.registerAllowedMethod("junit", [String])
        helper.registerAllowedMethod("logRotator", [Map])
        helper.registerAllowedMethod('mail', [Map])
        helper.registerAllowedMethod("node", [Closure])
        helper.registerAllowedMethod("node", [String, Closure])
        helper.registerAllowedMethod("pipelineTriggers", [List])
        helper.registerAllowedMethod("properties", [List])
        helper.registerAllowedMethod("pwd", [], { 'workspaceDirMocked' })
        helper.registerAllowedMethod("readFile", [String])
        helper.registerAllowedMethod('retry', [Integer, Closure]) { Integer count, Closure body ->
            c.delegate = delegate
            helper.callClosure(c)
        }
        helper.registerAllowedMethod("sh", [Map])
        helper.registerAllowedMethod("sh", [String])
        helper.registerAllowedMethod('skipDefaultCheckout')
        helper.registerAllowedMethod('sleep')
        helper.registerAllowedMethod('specific', [String])
        helper.registerAllowedMethod('sshPublisher', [Map], {true})
        helper.registerAllowedMethod('stash', [Map])
        helper.registerAllowedMethod("stage", [String])
        helper.registerAllowedMethod("stage", [String, Closure])
        helper.registerAllowedMethod("step", [Map])
        helper.registerAllowedMethod("string", [Map], stringInterceptor)
        helper.registerAllowedMethod('timeout', [Map])
        helper.registerAllowedMethod("timeout", [Map, Closure])
        helper.registerAllowedMethod('tool', [Map], { t -> "${t.name}_HOME" })
        helper.registerAllowedMethod("unstable", [String], { updateBuildStatus('UNSTABLE') })
        helper.registerAllowedMethod('unstash', [Map])
        helper.registerAllowedMethod('waitUntil', [Closure])
        helper.registerAllowedMethod("warnError", [String, Closure], { Closure c ->
            try {
                c.delegate = delegate
                helper.callClosure(c)
            } catch (ignored) {
                updateBuildStatus('UNSTABLE')
            }
        })
        helper.registerAllowedMethod("withCredentials", [List, Closure], withCredentialsInterceptor)
        helper.registerAllowedMethod('withCredentials', [Map, Closure])
        helper.registerAllowedMethod('writeFile', [Map])
        helper.registerAllowedMethod("ws", [String, Closure])
    }

    void setVariables() {
        binding.setVariable('currentBuild', [
            absoluteUrl: 'http://example.com/dummy',
            buildVariables: [:],
            changeSets: [],
            currentResult: 'SUCCESS',
            description: 'dummy',
            displayName: '#1',
            duration: 1,
            durationString: '1 ms',
            fullDisplayName: 'dummy #1',
            fullProjectName: 'dummy',
            id: '1',
            keepLog: false,
            nextBuild: null,
            number: 1,
            previousBuild: null,
            projectName: 'dummy',
            result: 'SUCCESS',
            startTimeInMillis: 1,
            timeInMillis: 1,
            upstreamBuilds: [],
        ])
        binding.setVariable('env',[:])
    }

    /**
     * Updates the build status.
     * Can be useful when mocking a jenkins method.
     * @param status job status to set
     */
    void updateBuildStatus(String status) {
        binding.getVariable('currentBuild').result = status
    }

    /**
     * Loads without running the script by its name/path, returning the Script
     * @param scriptName script name or path
     * @return script object
     */
    Script loadScript(String scriptName) {
        if (!helper.isInitialized()) {
            throw new IllegalStateException("Helper is not initialized: Call setUp() before tests.")
        }
        return helper.loadScript(scriptName, this.binding)
    }

    /**
     * Loads and runs the script by its name/path
     * @param scriptName script name or path
     * @return the return value of the script
     */
    Object runScript(String scriptName) {
        if (!helper.isInitialized()) {
            throw new IllegalStateException("Helper is not initialized: Call setUp() before tests.")
        }
        return helper.runScript(scriptName, this.binding)
    }

    /**
     * Run the script object
     * @param script Script object
     * @return the return value of the script
     */
    Object runScript(Script script) {
        return helper.runScript(script)
    }

    @Memoized
    String callStackDump() {
        return helper.callStack.stream()
                     .map { it -> it.toString() }
                     .collect(joining('\n'))
    }

    void printCallStack() {
        if (!Boolean.parseBoolean(System.getProperty("printstack.disabled"))) {
            println callStackDump()
        }
    }

    /**
     * Asserts the job status is FAILURE.
     * Please check the mocks update this status when necessary.
     * @See # updateBuildStatus ( String )
     */
    void assertJobStatusFailure() {
        assertJobStatus('FAILURE')
    }

    /**
     * Asserts the job status is UNSTABLE.
     * Please check the mocks update this status when necessary
     * @See # updateBuildStatus ( String )
     */
    void assertJobStatusUnstable() {
        assertJobStatus('UNSTABLE')
    }

    /**
     * Asserts the job status is SUCCESS.
     * Please check the mocks update this status when necessary
     * @See # updateBuildStatus ( String )
     */
    void assertJobStatusSuccess() {
        assertJobStatus('SUCCESS')
    }

    private assertJobStatus(String status) {
        assertThat(binding.getVariable('currentBuild').result).isEqualTo(status)
    }

    AbstractCharSequenceAssert assertCallStack() {
        return assertThat(callStackDump())
    }

    void assertCallStackContains(String text) {
        assertCallStack().contains(text)
    }


    /**
     * Helper for adding a params value in tests
     */
    void addParam(String name, Object val, Boolean overWrite = false) {
        Map params = binding.getVariable('params') as Map
        if (params == null) {
            params = [:]
            binding.setVariable('params', params)
        }
        if (params[name] == null || overWrite) {
            params[name] = val
        }
    }


    /**
     * Helper for adding a environment value in tests
     */
    void addEnvVar(String name, String val) {
        Map env = binding.getVariable('env') as Map
        if (env == null) {
            env = [:]
            binding.setVariable('env', env)
        }
        env[name] = val
    }

    void addCredential(String key, String val) {
        Map credentials = binding.getVariable('credentials') as Map
        if (credentials == null) {
            credentials = [:]
            binding.setVariable('credentials', credentials)
        }
        credentials[key] = val
    }
}
