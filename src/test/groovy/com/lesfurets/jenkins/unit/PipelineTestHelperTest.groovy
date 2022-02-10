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
    void readFile() {
        // given:
        def helper = new PipelineTestHelper()
        helper.addFileExistsMock('test', true)

        // when:
        def result = helper.fileExists('test')

        // then:
        Assertions.assertThat(result).isTrue()
    }

    @Test
    void readFileNotMocked() {
        // given:
        def helper = new PipelineTestHelper()

        // when:
        def result = helper.fileExists('test')

        // then:
        Assertions.assertThat(result).isFalse()
    }

    @Test
    void readFileWithMap() {
        // given:
        def helper = new PipelineTestHelper()
        helper.addReadFileMock('test', 'contents')

        // when:
        def output = helper.readFile(file: 'test')

        // then:
        Assertions.assertThat(output).isEqualTo('contents')
    }

    @Test
    void readFileWithNoMockOutput() {
        // given:
        def helper = new PipelineTestHelper()

        // when:
        def output = helper.readFile('test')

        // then:
        Assertions.assertThat(output).isEqualTo('')
    }

    @Test
    void runSh() {
        // given:
        def helper = new PipelineTestHelper()
        helper.addShMock('pwd', '/foo/bar', 0)

        // when:
        def output = helper.runSh('pwd')

        // then:
        Assertions.assertThat(output).isNull()
    }

    @Test
    void runShWithScriptFailure() {
        // given:
        def helper = new PipelineTestHelper()
        helper.addShMock('evil', '/foo/bar', 666)
        Exception caught = null

        // when:
        try {
            helper.runSh('evil')
        } catch (e) {
            caught = e
        }

        // then: Exception raised
        Assertions.assertThat(caught).isNotNull()
        Assertions.assertThat(caught.message).isEqualTo('script returned exit code 666')
    }

    @Test
    void runShWithStdout() {
        // given:
        def helper = new PipelineTestHelper()
        helper.addShMock('pwd', '/foo/bar', 0)

        // when:
        def output = helper.runSh(returnStdout: true, script: 'pwd')

        // then:
        Assertions.assertThat(output).isEqualTo('/foo/bar')
    }

    @Test(expected = Exception)
    void runShWithStdoutFailure() {
        // given:
        def helper = new PipelineTestHelper()
        helper.addShMock('pwd', '/foo/bar', 1)

        // when:
        helper.runSh(returnStdout: true, script: 'pwd')

        // then: Exception raised
    }

    @Test
    void runShWithReturnCode() {
        // given:
        def helper = new PipelineTestHelper()
        helper.addShMock('pwd', '/foo/bar', 0)

        // when:
        def output = helper.runSh(returnStatus: true, script: 'pwd')

        // then:
        Assertions.assertThat(output).isEqualTo(0)
    }

    @Test
    void runShWithNonZeroReturnCode() {
        // given:
        def helper = new PipelineTestHelper()
        helper.addShMock('evil', '/foo/bar', 666)

        // when:
        def output = helper.runSh(returnStatus: true, script: 'evil')

        // then:
        Assertions.assertThat(output).isEqualTo(666)
    }

    @Test
    void runShWithCallback() {
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
    void runShWithCallbackScriptFailure() {
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
    void runShWithCallbackStdout() {
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
    void runShWithCallbackReturnCode() {
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
    void runShWithCallbackNonZeroReturnCode() {
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
    void runShWithCallbackOutputNotMap() {
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
    void runShWithCallbackNoStdoutKey() {
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
    void runShWithCallbackNoExitValueKey() {
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
    void runShWithoutMockOutput() {
        // given:
        def helper = new PipelineTestHelper()

        // when:
        def output = helper.runSh('unregistered-mock-output')

        // then:
        Assertions.assertThat(output).isNull()
    }

    @Test()
    void runShWithoutMockOutputAndReturnStatus() {
        // given:
        def helper = new PipelineTestHelper()

        // when:
        def output = helper.runSh(returnStatus: true, script: 'unregistered-mock-output')

        // then:
        Assertions.assertThat(output).isEqualTo(0)
    }

    @Test()
    void runShWithoutMockOutputAndReturnStdout() {
        // given:
        def helper = new PipelineTestHelper()

        // when:
        def output = helper.runSh(returnStdout: true, script: 'unregistered-mock-output')

        // then:
        Assertions.assertThat(output).isEqualTo('')
    }

    @Test(expected = IllegalArgumentException)
    void runShWithBothStatusAndStdout() {
        // given:
        def helper = new PipelineTestHelper()

        // when:
        helper.runSh(returnStatus: true, returnStdout: true, script: 'invalid')

        // then: Exception raised
    }

}
