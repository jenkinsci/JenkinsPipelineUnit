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
        helper.registerAllowedMethod('booleanParam', [Map], paramInterceptor)
        helper.registerAllowedMethod('checkout', [Closure])
        helper.registerAllowedMethod('credentials', [String], { String credName ->
            return binding.getVariable('credentials')[credName]
        })
        helper.registerAllowedMethod('cron', [String])
        helper.registerAllowedMethod(method("pipeline", Closure), pipelineInterceptor)
        helper.registerAllowedMethod('pollSCM', [String])
        helper.registerAllowedMethod('script', [Closure])
        helper.registerAllowedMethod('skipDefaultCheckout')
        helper.registerAllowedMethod('string', [Map], paramInterceptor)
        helper.registerAllowedMethod('timeout', [Integer, Closure])
        helper.registerAllowedMethod('timestamps')
        helper.registerAllowedMethod('usernamePassword', [Map], { creds -> return creds })
        binding.setVariable('credentials', [:])
        binding.setVariable('params', [:])
    }
}
