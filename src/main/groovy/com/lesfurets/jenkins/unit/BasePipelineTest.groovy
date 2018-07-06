package com.lesfurets.jenkins.unit

import static org.assertj.core.api.Assertions.assertThat

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
        helper.registerAllowedMethod("stage", [String.class, Closure.class], null)
        helper.registerAllowedMethod("stage", [String.class], null)
        helper.registerAllowedMethod("node", [String.class, Closure.class], null)
        helper.registerAllowedMethod("node", [Closure.class], null)
        helper.registerAllowedMethod("sh", [String.class], null)
        helper.registerAllowedMethod("sh", [Map.class], null)
        helper.registerAllowedMethod("checkout", [Map.class], null)
        helper.registerAllowedMethod("echo", [String.class], null)
        helper.registerAllowedMethod("timeout", [Map.class, Closure.class], null)
        helper.registerAllowedMethod("step", [Map.class], null)
        helper.registerAllowedMethod("input", [String.class], null)
        helper.registerAllowedMethod("gitlabCommitStatus", [String.class, Closure.class], { String name, Closure c ->
            c.delegate = delegate
            helper.callClosure(c)
        })
        helper.registerAllowedMethod("gitlabBuilds", [Map.class, Closure.class], null)
        helper.registerAllowedMethod("logRotator", [Map.class], null)
        helper.registerAllowedMethod("buildDiscarder", [Object.class], null)
        helper.registerAllowedMethod("pipelineTriggers", [List.class], null)
        helper.registerAllowedMethod("properties", [List.class], null)
        helper.registerAllowedMethod("dir", [String.class, Closure.class], null)
        helper.registerAllowedMethod("archiveArtifacts", [Map.class], null)
        helper.registerAllowedMethod("junit", [String.class], null)
        helper.registerAllowedMethod("readFile", [String.class], null)
        helper.registerAllowedMethod("disableConcurrentBuilds", [], null)
        helper.registerAllowedMethod("gatlingArchive", [], null)
        helper.registerAllowedMethod("string", [Map.class], stringInterceptor)
        helper.registerAllowedMethod("withCredentials", [List.class, Closure.class], withCredentialsInterceptor)
        helper.registerAllowedMethod("error", [String.class], { updateBuildStatus('FAILURE') })
        helper.registerAllowedMethod("unstable", [String.class], { updateBuildStatus('UNSTABLE') })
        helper.registerAllowedMethod("warnError", [String.class, Closure.class], { Closure c ->
            try {
                c.delegate = delegate
                helper.callClosure(c)
            } catch (ignored) {
                updateBuildStatus('UNSTABLE')
            }
        })
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

    void printCallStack() {
        if (!Boolean.parseBoolean(System.getProperty("printstack.disabled"))) {
            helper.callStack.each {
                println it
            }
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

}
