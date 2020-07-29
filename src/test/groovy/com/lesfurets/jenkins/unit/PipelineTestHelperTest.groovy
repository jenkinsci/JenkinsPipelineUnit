package com.lesfurets.jenkins.unit

import org.assertj.core.api.Assertions
import org.junit.Test

class PipelineTestHelperTest {

    @Test
    void testRegisterAllowedMethodWithoutArgs() {
        // given:
        def helper = new PipelineTestHelper()
        def closure = { println 'withoutArgs' }
        helper.registerAllowedMethod('withoutArgs', closure)

        // when:
        Map.Entry<MethodSignature, Closure> allowedMethodEntry = helper.getAllowedMethodEntry('withoutArgs')

        // then:
        Assertions.assertThat(allowedMethodEntry.getKey().getArgs().size()).isEqualTo(0)
        Assertions.assertThat(allowedMethodEntry.getValue()).isEqualTo(closure)
    }

    @Test
    void testRegisterAllowedMethodEmptyArgs() {
        // given:
        def helper = new PipelineTestHelper()
        def closure = { println 'emptyArgsList' }
        helper.registerAllowedMethod('emptyArgsList', closure)

        // when:
        Map.Entry<MethodSignature, Closure> allowedMethodEntry = helper.getAllowedMethodEntry('emptyArgsList')

        // then:
        Assertions.assertThat(allowedMethodEntry.getKey().getArgs().size()).isEqualTo(0)
        Assertions.assertThat(allowedMethodEntry.getValue()).isEqualTo(closure)
    }

    @Test
    void readFile() throws Exception {
        // given:
        def helper = new PipelineTestHelper()
        helper.addReadFileMock('test', 'contents')

        // when:
        def output = helper.readFile('test')

        // then:
        Assertions.assertThat(output).isEqualTo('contents')
    }

    @Test
    void readFileWithMap() throws Exception {
        // given:
        def helper = new PipelineTestHelper()
        helper.addReadFileMock('test', 'contents')

        // when:
        def output = helper.readFile(file: 'test')

        // then:
        Assertions.assertThat(output).isEqualTo('contents')
    }

    @Test
    void readFileWithNoMockOutput() throws Exception {
        // given:
        def helper = new PipelineTestHelper()

        // when:
        def output = helper.readFile('test')

        // then:
        Assertions.assertThat(output).isEqualTo('')
    }

}