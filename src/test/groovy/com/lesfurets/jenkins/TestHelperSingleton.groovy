package com.lesfurets.jenkins

import com.lesfurets.jenkins.unit.BasePipelineTest
import com.lesfurets.jenkins.unit.HelperSingleton
import com.lesfurets.jenkins.unit.PipelineTestHelper
import org.junit.AfterClass
import org.junit.Before
import org.junit.BeforeClass
import org.junit.Test

import static com.lesfurets.jenkins.unit.global.lib.LibraryConfiguration.library
import static com.lesfurets.jenkins.unit.global.lib.LocalSource.localSource
import static org.assertj.core.api.Assertions.assertThat

class TestHelperSingleton extends BasePipelineTest {

    @BeforeClass
    static void beforeClass() {
        HelperSingleton.singletonInstance = new PipelineTestHelper()

        String sharedLibs = this.class.getResource('/libs').getFile()

        def library = library().name('commons')
                .defaultVersion("master")
                .allowOverride(true)
                .implicit(true)
                .targetPath(sharedLibs)
                .retriever(localSource(sharedLibs))
                .build()

        HelperSingleton.singletonInstance.registerSharedLibrary(library)
    }

    @Override
    @Before
    void setUp() throws Exception {
        scriptRoots += 'src/test/jenkins'
        super.setUp()
    }

    @Test
    void staticHelperTestRunScript() throws Exception {

        assertThat(helper.isInitialized())

        assertThat(helper == HelperSingleton.singletonInstance)

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

    @AfterClass
    static void tearDown() {
        HelperSingleton.invalidate()
    }

}
