package com.lesfurets.jenkins.unit

import org.assertj.core.api.Assertions
import org.junit.Test;

class PipelineTestHelperTest {

    @Test
    void testRegisterAllowedMethodWithoutArgs() {
        def helper = new PipelineTestHelper()
        def closure = { println 'withoutArgs'}
        helper.registerAllowedMethod('withoutArgs', closure)

        Map.Entry<MethodSignature, Closure> allowedMethodEntry = helper.getAllowedMethodEntry('withoutArgs')

        Assertions.assertThat(allowedMethodEntry.getKey().getArgs().size()).isEqualTo(0)
        Assertions.assertThat(allowedMethodEntry.getValue()).isEqualTo(closure)
    }

    @Test
    void testRegisterAllowedMethodEmptyArgs() {
        // given:
        def helper = new PipelineTestHelper()
        def closure = { println 'emptyArgsList'}
        helper.registerAllowedMethod('emptyArgsList', closure)

        // when:
        Map.Entry<MethodSignature, Closure> allowedMethodEntry = helper.getAllowedMethodEntry('emptyArgsList')

        // then:
        Assertions.assertThat(allowedMethodEntry.getKey().getArgs().size()).isEqualTo(0)
        Assertions.assertThat(allowedMethodEntry.getValue()).isEqualTo(closure)
    }

}