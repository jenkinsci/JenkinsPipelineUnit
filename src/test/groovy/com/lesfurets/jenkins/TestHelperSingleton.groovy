package com.lesfurets.jenkins

import com.lesfurets.jenkins.unit.BasePipelineTest
import com.lesfurets.jenkins.unit.PipelineTestHelper
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

import static com.lesfurets.jenkins.unit.global.lib.LibraryConfiguration.library
import static com.lesfurets.jenkins.unit.global.lib.LocalSource.localSource
import static org.assertj.core.api.Assertions.assertThat

class TestHelperSingleton extends BasePipelineTest {

    static PipelineTestHelper HELPER = new PipelineTestHelper()

    TestHelperSingleton() {
        super(HELPER)
    }

    @Override
    @BeforeEach
    void setUp() throws Exception {
        scriptRoots += 'src/test/jenkins'

        String sharedLibs = this.class.getResource('/libs').getFile()

        def library = library().name('commons')
                .defaultVersion("master")
                .allowOverride(true)
                .implicit(true)
                .targetPath(sharedLibs)
                .retriever(localSource(sharedLibs))
                .build()

        HELPER.registerSharedLibrary(library)

        super.setUp()
    }

    @Test
    void staticHelperTestRunScript() throws Exception {

        assertThat(helper.isInitialized())

        assertThat(helper == HELPER)

        boolean exception = false
        try {
            def script = runScript("job/library/libraryJob.jenkins")
            script.execute()
            printCallStack()
        } catch (e) {
            e.printStackTrace()
            exception = true
        }
        assertThat(false).isEqualTo(exception)
    }

}
