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

    @Test
    void runSh() throws Exception {
        // given:
        def helper = new PipelineTestHelper()
        helper.addShMock('pwd', '/foo/bar', 0)

        // when:
        def output = helper.runSh('pwd')

        // then:
        Assertions.assertThat(output).isNull()
    }

    @Test(expected = Exception)
    void runShWithScriptFailure() throws Exception {
        // given:
        def helper = new PipelineTestHelper()
        helper.addShMock('evil', '/foo/bar', 666)

        // when:
        helper.runSh('evil')

        // then: Exception raised
    }

    @Test
    void runShWithStdout() throws Exception {
        // given:
        def helper = new PipelineTestHelper()
        helper.addShMock('pwd', '/foo/bar', 0)

        // when:
        def output = helper.runSh(returnStdout: true, script: 'pwd')

        // then:
        Assertions.assertThat(output).isEqualTo('/foo/bar')
    }

    @Test
    void runShWithReturnCode() throws Exception {
        // given:
        def helper = new PipelineTestHelper()
        helper.addShMock('pwd', '/foo/bar', 0)

        // when:
        def output = helper.runSh(returnStatus: true, script: 'pwd')

        // then:
        Assertions.assertThat(output).isEqualTo(0)
    }

    @Test
    void runShWithNonZeroReturnCode() throws Exception {
        // given:
        def helper = new PipelineTestHelper()
        helper.addShMock('evil', '/foo/bar', 666)

        // when:
        def output = helper.runSh(returnStatus: true, script: 'evil')

        // then:
        Assertions.assertThat(output).isEqualTo(666)
    }

    @Test
    void runShWithCallback() throws Exception {
        // given:
        def helper = new PipelineTestHelper()
        helper.addShMock('pwd') { script ->
            return [stdout: '/foo/bar', exitValue: 0]
        }

        // when:
        def output = helper.runSh('pwd')

        // then:
        Assertions.assertThat(output).isNull()
    }

    @Test(expected = Exception)
    void runShWithCallbackScriptFailure() throws Exception {
        // given:
        def helper = new PipelineTestHelper()
        helper.addShMock('evil') { script ->
            return [stdout: '/foo/bar', exitValue: 666]
        }

        // when:
        helper.runSh('evil')

        // then: Exception raised
    }

    @Test
    void runShWithCallbackStdout() throws Exception {
        // given:
        def helper = new PipelineTestHelper()
        helper.addShMock('pwd') { script ->
            return [stdout: '/foo/bar', exitValue: 0]
        }

        // when:
        def output = helper.runSh(returnStdout: true, script: 'pwd')

        // then:
        Assertions.assertThat(output).isEqualTo('/foo/bar')
    }

    @Test
    void runShWithCallbackReturnCode() throws Exception {
        // given:
        def helper = new PipelineTestHelper()
        helper.addShMock('pwd') { script ->
            return [stdout: '/foo/bar', exitValue: 0]
        }

        // when:
        def output = helper.runSh(returnStatus: true, script: 'pwd')

        // then:
        Assertions.assertThat(output).isEqualTo(0)
    }

    @Test
    void runShWithCallbackNonZeroReturnCode() throws Exception {
        // given:
        def helper = new PipelineTestHelper()
        helper.addShMock('pwd') { script ->
            return [stdout: '/foo/bar', exitValue: 666]
        }

        // when:
        def output = helper.runSh(returnStatus: true, script: 'pwd')

        // then:
        Assertions.assertThat(output).isEqualTo(666)
    }

    @Test(expected = IllegalArgumentException)
    void runShWithCallbackOutputNotMap() throws Exception {
        // given:
        def helper = new PipelineTestHelper()
        helper.addShMock('pwd') { script ->
            return 'invalid'
        }

        // when:
        helper.runSh(returnStatus: true, script: 'pwd')

        // then: Exception raised
    }

    @Test(expected = IllegalArgumentException)
    void runShWithCallbackNoStdoutKey() throws Exception {
        // given:
        def helper = new PipelineTestHelper()
        helper.addShMock('pwd') { script ->
            return [exitValue: 666]
        }

        // when:
        helper.runSh(returnStatus: true, script: 'pwd')

        // then: Exception raised
    }

    @Test(expected = IllegalArgumentException)
    void runShWithCallbackNoExitValueKey() throws Exception {
        // given:
        def helper = new PipelineTestHelper()
        helper.addShMock('pwd') { script ->
            return [stdout: '/foo/bar']
        }

        // when:
        helper.runSh(returnStatus: true, script: 'pwd')

        // then: Exception raised
    }

    @Test()
    void runShWithoutMockOutput() throws Exception {
        // given:
        def helper = new PipelineTestHelper()

        // when:
        def output = helper.runSh('unregistered-mock-output')

        // then:
        Assertions.assertThat(output).isEqualTo('\nbbb\nccc\n')
    }

    @Test()
    void runShWithoutMockOutputForGitRevParse() throws Exception {
        // given:
        def helper = new PipelineTestHelper()

        // when:
        def output = helper.runSh(returnStdout: true, script: 'git rev-parse HEAD')

        // then:
        Assertions.assertThat(output).isEqualTo('abcd123\n')
    }

    @Test(expected = IllegalArgumentException)
    void runShWithBothStatusAndStdout() throws Exception {
        // given:
        def helper = new PipelineTestHelper()

        // when:
        helper.runSh(returnStatus: true, returnStdout: true, script: 'invalid')

        // then: Exception raised
    }

}