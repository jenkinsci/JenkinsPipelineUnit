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

        helper.registerAllowedMethod('timestamps', [], null)


        helper.registerAllowedMethod('script', [Closure.class], null)
        helper.registerAllowedMethod('string', [Map.class], paramInterceptor)
        helper.registerAllowedMethod('booleanParam', [Map.class], paramInterceptor)

        helper.registerAllowedMethod('credentials', [String], { String credName ->
            return binding.getVariable('credentials')[credName]
        })
    }
}
