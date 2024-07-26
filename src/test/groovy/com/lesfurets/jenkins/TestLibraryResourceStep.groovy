package com.lesfurets.jenkins

import com.lesfurets.jenkins.unit.BasePipelineTest
import org.junit.Before
import org.junit.Test

import static com.lesfurets.jenkins.unit.global.lib.LibraryConfiguration.library
import static com.lesfurets.jenkins.unit.global.lib.LocalSource.localSource
import static org.assertj.core.api.Assertions.assertThat

class TestLibraryResourceStep extends BasePipelineTest {

    String sharedLibs = this.class.getResource('/libs').getFile()

    @Override
    @Before
    void setUp() throws Exception {
        scriptRoots += 'src/test/jenkins'
        super.setUp()

        def library = library()
                        .name('commons')
                        .defaultVersion('feature')
                        .allowOverride(true)
                        .implicit(false)
                        .targetPath(sharedLibs)
                        .retriever(localSource(sharedLibs))
                        .build()

        helper.registerSharedLibrary(library)
    }

    @Test
    void libraryResource_with_string() {
        // given: net/courtanet/jenkins/plaintext.utf8.txt

        // when: libraryResource 'net/courtanet/jenkins/plaintext.utf8.txt'
        runScript('job/library/test_libraryResource_with_string.jenkins')

        // then: Emojis encoded using UTF-8 should be remain when decoding using UTF-8
        assertThat(helper.callStack.find({ call -> call.methodName == 'echo' }).args[0])
                .isEqualTo('\uD83D\uDE03')
    }

    @Test
    void libraryResource_with_named_string() {
        // given: net/courtanet/jenkins/plaintext.utf8.txt

        // when: libraryResource resource: 'net/courtanet/jenkins/plaintext.utf8.txt'
        runScript('job/library/test_libraryResource_without_encoding.jenkins')

        // then:
        assertThat(helper.callStack.find({ call -> call.methodName == 'echo' }).args[0])
                .isEqualTo('\uD83D\uDE03')
    }

    @Test
    void libraryResource_with_encoding() {
        // given: net/courtanet/jenkins/plaintext.iso-8859-1.txt

        // when: libraryResource resource: 'net/courtanet/jenkins/plaintext.iso-8859-1.txt', encoding: 'ISO-8859-15'
        runScript('job/library/test_libraryResource_with_encoding.jenkins')

        // then: '¤' encoded using ISO-8859-1 should turn into '€' when decoding using ISO-8859-15
        assertThat(helper.callStack.find({ call -> call.methodName == 'echo' }).args[0])
                .isEqualTo('€')
    }

    @Test
    void libraryResource_as_base64() {
        // given: net/courtanet/jenkins/icon.png

        // when: libraryResource resource: 'net/courtanet/jenkins/icon.png', encoding: 'Base64'
        runScript('job/library/test_libraryResource_as_base64.jenkins')

        // then: Files encoded as binary data should be returned as Base64 when specified
        assertThat(helper.callStack.find({ call -> call.methodName == 'echo' }).args[0])
                .isEqualTo('iVBORw0KGgoAAAANSUhEUgAAABAAAAAQBAMAAADt3eJSAAAAElBMVEXu7u7d3d3l5eXV1dXPz8/JycnBqezIAAAAFklEQVQI12OgHhBxVhYAM1QCgQwyAQBLogEzdUEb2QAAAABJRU5ErkJggg==')
    }

}
