package com.lesfurets.jenkins.unit.declarative

import static com.lesfurets.jenkins.unit.MethodSignature.method

import com.lesfurets.jenkins.unit.BasePipelineTest

@groovy.transform.InheritConstructors
abstract class DeclarativePipelineTest extends BasePipelineTest {

    def pipelineInterceptor = { Closure closure ->
        DeclarativePipeline.createComponent(DeclarativePipeline, closure).execute(delegate)
    }

    def paramInterceptor = { Map desc ->
        addParam(desc.name, desc.defaultValue, false)
    }

    @Override
    void setUp() throws Exception {
        super.setUp()

        /**
         * Job params - may need to override in specific tests
         */
        binding.setVariable('params', [:])
        binding.setVariable('credentials', [:])

        helper.registerAllowedMethod(method("pipeline", Closure), pipelineInterceptor)

        helper.registerAllowedMethod('pollSCM', [String])
        helper.registerAllowedMethod('cron', [String])
        helper.registerAllowedMethod('timestamps')

        helper.registerAllowedMethod('skipDefaultCheckout')

        helper.registerAllowedMethod('script', [Closure])

        helper.registerAllowedMethod('timeout', [Integer, Closure])

        helper.registerAllowedMethod('waitUntil', [Closure])
        helper.registerAllowedMethod('writeFile', [Map])
        helper.registerAllowedMethod('build', [Map])
        helper.registerAllowedMethod('tool', [Map], { t -> "${t.name}_HOME" })

        helper.registerAllowedMethod('withCredentials', [Map, Closure])
        helper.registerAllowedMethod('withCredentials', [List, Closure])
        helper.registerAllowedMethod('usernamePassword', [Map], { creds -> return creds })

        helper.registerAllowedMethod('deleteDir')
        helper.registerAllowedMethod('pwd', [], { 'workspaceDirMocked' })

        helper.registerAllowedMethod('stash', [Map])
        helper.registerAllowedMethod('unstash', [Map])

        helper.registerAllowedMethod('checkout', [Closure])

        helper.registerAllowedMethod('string', [Map], paramInterceptor)
        helper.registerAllowedMethod('booleanParam', [Map], paramInterceptor)

        helper.registerAllowedMethod('withEnv', [List, Closure], { List list, Closure c ->

            list.each {
                //def env = helper.get
                def item = it.split('=')
                assert item.size() == 2, "withEnv list does not look right: ${list.toString()}"
                addEnvVar(item[0], item[1])
                c.delegate = binding
                c.call()
            }
        })

        helper.registerAllowedMethod('credentials', [String], { String credName ->
            return binding.getVariable('credentials')[credName]
        })
    }
}
