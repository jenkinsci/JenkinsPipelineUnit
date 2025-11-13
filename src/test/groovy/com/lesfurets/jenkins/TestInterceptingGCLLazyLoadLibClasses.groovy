package com.lesfurets.jenkins

import com.lesfurets.jenkins.unit.BasePipelineTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

import static com.lesfurets.jenkins.unit.global.lib.LibraryConfiguration.library
import static com.lesfurets.jenkins.unit.global.lib.ProjectSource.projectSource

class TestInterceptingGCLLazyLoadLibClasses extends BasePipelineTest {
    @Override
    @BeforeEach
    void setUp() throws Exception {
        scriptRoots += 'src/test/jenkins'
        super.setUp()
        helper.libLoader.preloadLibraryClasses = false
    }

    /**
    * 1. Load two libraries--one dependent on the other--with implicity
    * 2. Create instance of library class and pass instance to library vars step
    * 3. That vars step in turn creates instance of another library class
    *    and passes it to another library step
    * 4. Make sure interception of pipeline methods works propertly
    */
    @Test
    void test_cross_class_as_var_arg_implicit_lazy_load() throws Exception {
        //This does not factor much in the current test but does replicate the
        //use case in which the lazy load feature originated.
        helper.cloneArgsOnMethodCallRegistration = false

        //test_cross_class_as_var_arg_1 uses vars and classes in
        //test_cross_class_as_var_arg_2 so the latter has to be loaded first
        [
            "test_cross_class_as_var_arg_2",
            "test_cross_class_as_var_arg_1",
        ].each { libName ->
            final libDir = this.class.getResource("/libs/$libName").file
            final library = library().name(libName)
                                     .defaultVersion("master")
                                     .allowOverride(false)
                                     .implicit(true)
                                     .targetPath(libDir)
                                     .retriever(projectSource(libDir))
                                     .build()
            helper.registerSharedLibrary(library)
        }

        final pipeline = "test_var_with_lib_class_arg"
        runScript("job/library/cross_class_lazy_load/${pipeline}.jenkins")
        printCallStack()
        assertCallStackContains("""${pipeline}.monster1(org.test.Monster1""")
        assertCallStackContains("""monster1.monster2(org.test.extra.Monster2""")
        assertCallStackContains("""monster2.echo(Frankenstein's Monster all by itself is frightening)""")
        assertCallStackContains("""monster1.echo(Dracula and Frankenstein's Monster make quite a scary team)""")
    }
}
