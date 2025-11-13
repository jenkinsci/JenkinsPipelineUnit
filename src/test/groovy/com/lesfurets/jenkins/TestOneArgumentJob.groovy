package com.lesfurets.jenkins

import com.lesfurets.jenkins.unit.BaseRegressionTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

import static com.lesfurets.jenkins.unit.global.lib.LibraryConfiguration.library
import static com.lesfurets.jenkins.unit.global.lib.LocalSource.localSource

class TestOneArgumentJob extends BaseRegressionTest {
    
    String sharedLibs = this.class.getResource('/libs').getFile()

    @Override
    @BeforeEach
    void setUp() throws Exception {
        scriptRoots += 'src/test/jenkins'
        super.setUp()
        binding.setVariable('scm', [branch: 'master'])
    }

    @Test
    void should_run_script_with_one_argument() {
        def library = library().name('commons')
                        .defaultVersion("master")
                        .allowOverride(true)
                        .implicit(false)
                        .targetPath(sharedLibs)
                        .retriever(localSource(sharedLibs))
                        .build()
        helper.registerSharedLibrary(library)

        // when:
        runScript("job/library/test_lib_call_with_null.jenkins")

        // then:
        assertJobStatusSuccess()
        testNonRegression("should_run_script_with_one_argument")
    }
}
