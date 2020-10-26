package com.lesfurets.jenkins

import static com.lesfurets.jenkins.unit.global.lib.LibraryConfiguration.library
import static com.lesfurets.jenkins.unit.global.lib.LocalSource.localSource

import com.lesfurets.jenkins.unit.LibClassLoader
import com.lesfurets.jenkins.unit.BaseRegressionTest
import org.junit.Before
import org.junit.Test

import static org.junit.Assert.assertTrue

class TestOneArgumentJob extends BaseRegressionTest {
    
    String sharedLibs = this.class.getResource('/libs').getFile()

    @Override
    @Before
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
