package com.lesfurets.jenkins.helpers

import org.junit.Before

abstract class BasePipelineTest {

    PipelineTestHelper helper
    String[] roots = ["src/main/jenkins" , "./."]

    String extension = "jenkins"

    Class baseClass = MockPipelineScript.class

    Map<String, String> imports = ["NonCPS": "com.cloudbees.groovy.cps.NonCPS"]

    String baseScriptRoot = "production/jenkins/"

    Binding binding = new Binding()

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

    BasePipelineTest() {
        helper = new PipelineTestHelper()
        helper.setScriptRoots roots
        helper.setScriptExtension extension
        helper.setBaseClassloader this.class.classLoader
        helper.setScriptBaseClass baseClass
        helper.setImports imports
        helper.setBaseScriptRoot baseScriptRoot
    }

    @Before
    void setUp() throws Exception {
        helper.build()
        helper.registerAllowedMethod("gitlabCommitStatus", [String.class, Closure.class], null)
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
    }

    Script loadScript(String scriptName) {
        return helper.loadScript(scriptName, this.binding)
    }

    void printCallStack() {
        if (!Boolean.parseBoolean(System.getProperty("printstack.disabled"))) {
            helper.callStack.each {
                println it
            }
        }
    }
}
