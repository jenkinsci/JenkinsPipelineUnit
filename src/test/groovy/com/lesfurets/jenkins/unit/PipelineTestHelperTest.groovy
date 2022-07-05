package com.lesfurets.jenkins.unit

import static org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Test

class PipelineTestHelperTest {
    private PipelineTestHelper helper

    @Before
    void setUp() throws Exception {
        helper = new PipelineTestHelper()
    }

    @Test
    void testRegisterAllowedMethodWithoutArgs() {
        // given:
        def closure = { println 'withoutArgs' }
        helper.registerAllowedMethod('withoutArgs', closure)

        // when:
        Map.Entry<MethodSignature, Closure> allowedMethodEntry = helper.getAllowedMethodEntry('withoutArgs')

        // then:
        assertThat(allowedMethodEntry.getKey().getArgs().size()).isEqualTo(0)
        assertThat(allowedMethodEntry.getValue()).isEqualTo(closure)
    }

    @Test
    void testRegisterAllowedMethodEmptyArgs() {
        // given:
        def closure = { println 'emptyArgsList' }
        helper.registerAllowedMethod('emptyArgsList', closure)

        // when:
        Map.Entry<MethodSignature, Closure> allowedMethodEntry = helper.getAllowedMethodEntry('emptyArgsList')

        // then:
        assertThat(allowedMethodEntry.getKey().getArgs().size()).isEqualTo(0)
        assertThat(allowedMethodEntry.getValue()).isEqualTo(closure)
    }

    @Test
    void readFile() {
        // given:
        helper.addFileExistsMock('test', true)

        // when:
        def result = helper.fileExists('test')

        // then:
        assertThat(result).isTrue()
    }

    @Test
    void readFileNotMocked() {
        // given:

        // when:
        def result = helper.fileExists('test')

        // then:
        assertThat(result).isFalse()
    }

    @Test
    void readFileWithMap() {
        // given:
        helper.addReadFileMock('test', 'contents')

        // when:
        def output = helper.readFile(file: 'test')

        // then:
        assertThat(output).isEqualTo('contents')
    }

    @Test
    void readFileWithNoMockOutput() {
        // given:

        // when:
        def output = helper.readFile('test')

        // then:
        assertThat(output).isEqualTo('')
    }

    @Test
    void runSh() {
        // given:
        helper.addShMock('pwd', '/foo/bar', 0)

        // when:
        def output = helper.runSh('pwd')

        // then:
        assertThat(output).isNull()
    }

    @Test
    void runShWithScriptFailure() {
        // given:
        helper.addShMock('evil', '/foo/bar', 666)
        Exception caught = null

        // when:
        try {
            helper.runSh('evil')
        } catch (e) {
            caught = e
        }

        // then: Exception raised
        assertThat(caught).isNotNull()
        assertThat(caught.message).isEqualTo('script returned exit code 666')
    }

    @Test
    void runShWithStdout() {
        // given:
        helper.addShMock('pwd', '/foo/bar', 0)

        // when:
        def output = helper.runSh(returnStdout: true, script: 'pwd')

        // then:
        assertThat(output).isEqualTo('/foo/bar')
    }

    @Test(expected = Exception)
    void runShWithStdoutFailure() {
        // given:
        helper.addShMock('pwd', '/foo/bar', 1)

        // when:
        helper.runSh(returnStdout: true, script: 'pwd')

        // then: Exception raised
    }

    @Test
    void runShWithReturnCode() {
        // given:
        helper.addShMock('pwd', '/foo/bar', 0)

        // when:
        def output = helper.runSh(returnStatus: true, script: 'pwd')

        // then:
        assertThat(output).isEqualTo(0)
    }

    @Test
    void runShWithNonZeroReturnCode() {
        // given:
        helper.addShMock('evil', '/foo/bar', 666)

        // when:
        def output = helper.runSh(returnStatus: true, script: 'evil')

        // then:
        assertThat(output).isEqualTo(666)
    }

    @Test
    void runShWithCallback() {
        // given:
        helper.addShMock('pwd') { script ->
            return [stdout: '/foo/bar', exitValue: 0]
        }

        // when:
        def output = helper.runSh('pwd')

        // then:
        assertThat(output).isNull()
    }

    @Test(expected = Exception)
    void runShWithCallbackScriptFailure() {
        // given:
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
        helper.addShMock('pwd') { script ->
            return [stdout: '/foo/bar', exitValue: 0]
        }

        // when:
        def output = helper.runSh(returnStdout: true, script: 'pwd')

        // then:
        assertThat(output).isEqualTo('/foo/bar')
    }

    @Test
    void runShWithCallbackReturnCode() {
        // given:
        helper.addShMock('pwd') { script ->
            return [stdout: '/foo/bar', exitValue: 0]
        }

        // when:
        def output = helper.runSh(returnStatus: true, script: 'pwd')

        // then:
        assertThat(output).isEqualTo(0)
    }

    @Test
    void runShWithCallbackNonZeroReturnCode() {
        // given:
        helper.addShMock('pwd') { script ->
            return [stdout: '/foo/bar', exitValue: 666]
        }

        // when:
        def output = helper.runSh(returnStatus: true, script: 'pwd')

        // then:
        assertThat(output).isEqualTo(666)
    }

    @Test(expected = IllegalArgumentException)
    void runShWithCallbackOutputNotMap() {
        // given:
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
        helper.addShMock('pwd') { script ->
            return [stdout: '/foo/bar']
        }

        // when:
        helper.runSh(returnStatus: true, script: 'pwd')

        // then: Exception raised
    }

    @Test()
    void runShWithPattern() {
        // given:
        def helper = new PipelineTestHelper()
        helper.addShMock(~/echo\s(.*)/) { String script, String arg ->
            assertThat(script).isEqualTo('echo foo')
            assertThat(arg).isEqualTo('foo')
            return [stdout: '', exitValue: 2]
        }

        // when:
        def status = helper.runSh(returnStatus: true, script: 'echo foo')

        // then:
        assertThat(status).isEqualTo(2)
    }

    @Test()
    void runShWithPatternStatus() {
        // given:
        def helper = new PipelineTestHelper()
        helper.addShMock(~/echo\s(.*)/, 'mock-output', 777)

        // when:
        def status = helper.runSh(returnStatus: true, script: 'echo foo')

        // then:
        assertThat(status).isEqualTo(777)
    }

    @Test()
    void runShWithPatternStdout() {
        // given:
        def helper = new PipelineTestHelper()
        helper.addShMock(~/echo\s(.*)/, 'mock-output', 0)

        // when:
        def status = helper.runSh(returnStdout: true, script: 'echo foo')

        // then:
        assertThat(status).isEqualTo('mock-output')
    }

    @Test()
    void runShWithDefaultPattern() {
        // given:
        helper.addShMock(~/.*/) { String script, String ...args ->
            assertThat(script).isEqualTo('echo foo')
            assertThat(args as List).isEqualTo([])
            return [stdout: '', exitValue: 2]
        }

        // when:
        def status = helper.runSh(returnStatus: true, script: 'echo foo')

        // then:
        assertThat(status).isEqualTo(2)
    }

    @Test()
    void runShWithoutMockOutput() {
        // given:

        // when:
        def output = helper.runSh('unregistered-mock-output')

        // then:
        assertThat(output).isNull()
    }

    @Test()
    void runShWithoutMockOutputAndReturnStatus() {
        // given:

        // when:
        def output = helper.runSh(returnStatus: true, script: 'unregistered-mock-output')

        // then:
        assertThat(output).isEqualTo(0)
    }

    @Test()
    void runShWithoutMockOutputAndReturnStdout() {
        // given:

        // when:
        def output = helper.runSh(returnStdout: true, script: 'unregistered-mock-output')

        // then:
        assertThat(output).isEqualTo('')
    }

    @Test()
    void runShWithDefaultHandler() {
        // given:
        helper.addShMock('command', 'ignored', 0)
        helper.addShMock(null, 'default', 1)
        helper.addShMock('pwd', 'ignored', 2)

        // when:
        def status = helper.runSh(script: 'command', returnStatus: true)

        // then:
        assertThat(status).isEqualTo(1)
    }

    @Test()
    void runShWithOverriddenHandler() {
        // given:
        helper.addShMock('command', 'base', 1)
        helper.addShMock('command', 'override', 2)

        // when:
        def status = helper.runSh(script: 'command', returnStatus: true)

        // then:
        assertThat(status).isEqualTo(2)
    }

    @Test(expected = IllegalArgumentException)
    void runShWithBothStatusAndStdout() {
        // given:

        // when:
        helper.runSh(returnStatus: true, returnStdout: true, script: 'invalid')

        // then: Exception raised
    }

}
