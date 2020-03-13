package com.lesfurets.jenkins

import org.junit.Before
import org.junit.Test

import static com.lesfurets.jenkins.unit.global.lib.LibraryConfiguration.library
import static com.lesfurets.jenkins.unit.global.lib.ProjectSource.projectSource

class TestInterceptingGCL extends BasePipelineTest {
    @Override
    @Before
    void setUp() throws Exception {
        scriptRoots += 'src/test/jenkins'
        super.setUp()
    }
    String sharedLibs = this.class
        .getResource('/libs/test_not_serrializable_cps')
        .getFile()


    @Test
    void test_vars_interop_library_loaded_with_implicity() throws Exception {
        def library = library().name('test_cross_vars')
                        .defaultVersion("master")
                        .allowOverride(false)
                        .implicit(true)
                        .targetPath(sharedLibs)
                        .retriever(localSource(sharedLibs))
                        .build()
        helper.registerSharedLibrary(library)

        helper.registerAllowedMethod("library", [String.class], {String expression ->
            helper.getLibLoader().loadLibrary(expression)
            return new LibClassLoader(helper,null)
        })

        runScript('job/library/test_cross_vars_implicity.jenkins')
    }

    @Test
    void test_vars_interop_no_implicit_library() throws Exception {
        def library = library().name('test_cross_vars')
                        .defaultVersion("master")
                        .allowOverride(false)
                        .implicit(false)
                        .targetPath(sharedLibs)
                        .retriever(localSource(sharedLibs))
                        .build()
        helper.registerSharedLibrary(library)

        helper.registerAllowedMethod("library", [String.class], {String expression ->
            helper.getLibLoader().loadLibrary(expression)
            return new LibClassLoader(helper,null)
        })

        runScript('job/library/test_cross_vars.jenkins')
    }
}
