package com.lesfurets.jenkins

import com.lesfurets.jenkins.unit.LibClassLoader
import com.lesfurets.jenkins.unit.BasePipelineTest

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
    String sharedLibOne = this.class
        .getResource('/libs/test_cross_vars_usage')
        .getFile()

    String sharedLibTwo = this.class
        .getResource('/libs/test_cross_class_usage')
        .getFile()

    @Test
    void test_vars_interop_library_loaded_with_implicity() throws Exception {
        def library = library().name('test_cross_vars_uno')
                        .defaultVersion("alpha")
                        .allowOverride(false)
                        .implicit(true)
                        .targetPath(sharedLibOne)
                        .retriever(projectSource(sharedLibOne))
                        .build()
        helper.registerSharedLibrary(library)

        runScript('job/library/cross_vars/test_implicity.jenkins')
        printCallStack()
        assertCallStack().contains("""test_implicity.methodAB()""")
        assertCallStack().contains("""methodAB.methodA()""")
        assertCallStack().contains("""methodA.echo(I'm A)""")
        assertCallStack().contains("""methodAB.methodB()""")
        assertCallStack().contains("""methodB.echo(I'm B)""")

    }

    @Test
    void test_vars_interop_no_implicity_dynamic() throws Exception {
        def library = library().name('test_cross_vars_dos')
                        .defaultVersion("beta")
                        .allowOverride(false)
                        .implicit(false)
                        .targetPath(sharedLibOne)
                        .retriever(projectSource(sharedLibOne))
                        .build()
        helper.registerSharedLibrary(library)

        runScript('job/library/cross_vars/test_dynamic.jenkins')
        printCallStack()
        assertCallStack().contains("""test_dynamic.methodAB()""")
        assertCallStack().contains("""methodAB.methodA()""")
        assertCallStack().contains("""methodA.echo(I'm A)""")
        assertCallStack().contains("""methodAB.methodB()""")
        assertCallStack().contains("""methodB.echo(I'm B)""")
    }

    @Test
    void test_vars_interop_no_implicity_annotation() throws Exception {
        def library = library().name('test_cross_vars_tres')
                        .defaultVersion("gamma")
                        .allowOverride(false)
                        .implicit(false)
                        .targetPath(sharedLibOne)
                        .retriever(projectSource(sharedLibOne))
                        .build()
        helper.registerSharedLibrary(library)

        runScript('job/library/cross_vars/test_annotation.jenkins')
        printCallStack()
        assertCallStack().contains("""test_annotation.methodAB()""")
        assertCallStack().contains("""methodAB.methodA()""")
        assertCallStack().contains("""methodA.echo(I'm A)""")
        assertCallStack().contains("""methodAB.methodB()""")
        assertCallStack().contains("""methodB.echo(I'm B)""")
    }


    @Test
    void test_lib_call_shell_no_implicity_dynamic() throws Exception {
        def library = library().name('test_lib_call_shell_uno')
                        .defaultVersion("alpha")
                        .allowOverride(false)
                        .implicit(false)
                        .targetPath(sharedLibOne)
                        .retriever(projectSource(sharedLibOne))
                        .build()
        helper.registerSharedLibrary(library)

        runScript('job/library/test_lib_call_shell_dynamic.jenkins')
        printCallStack()
        assertCallStack().contains("""LaClass.sh(echo 'run ls -la with bash')""")
    }

    @Test
    void test_lib_call_shell_no_implicity_annotation() throws Exception {
        def library = library().name('test_lib_call_shell_dos')
                        .defaultVersion("beta")
                        .allowOverride(false)
                        .implicit(false)
                        .targetPath(sharedLibOne)
                        .retriever(projectSource(sharedLibOne))
                        .build()
        helper.registerSharedLibrary(library)

        runScript('job/library/test_lib_call_shell_annotation.jenkins')
        printCallStack()
        assertCallStack().contains("""LaClass.sh(echo 'run ls -la with bash')""")
    }

    @Test
    void test_cross_class_interop_library_loaded_with_implicity() throws Exception {
        def library = library().name('test_cross_class_uno')
                        .defaultVersion("alpha")
                        .allowOverride(false)
                        .implicit(true)
                        .targetPath(sharedLibTwo)
                        .retriever(projectSource(sharedLibTwo))
                        .build()
        helper.registerSharedLibrary(library)

        runScript('job/library/cross_class/test_implicity.jenkins')
        printCallStack()
        assertCallStack().contains("""ClassA.methodA()""")
        assertCallStack().contains("""ClassA.sh(echo 'ClassA: I'm field of A')""")
        assertCallStack().contains("""ClassB.methodB()""")
        assertCallStack().contains("""ClassB.sh(echo 'ClassB: I'm field of B')""")
    }

    @Test
    void test_class_interop_no_implicity_dynamic() throws Exception {
        def library = library().name('test_cross_class_dos')
                        .defaultVersion("beta")
                        .allowOverride(false)
                        .implicit(false)
                        .targetPath(sharedLibTwo)
                        .retriever(projectSource(sharedLibTwo))
                        .build()
        helper.registerSharedLibrary(library)

        runScript('job/library/cross_class/test_dynamic.jenkins')
        printCallStack()
        assertCallStack().contains("""ClassA.methodA()""")
        assertCallStack().contains("""ClassA.sh(echo 'ClassA: I'm field of A')""")
        assertCallStack().contains("""ClassB.methodB()""")
        assertCallStack().contains("""ClassB.sh(echo 'ClassB: I'm field of B')""")
    }

    @Test
    void test_class_interop_no_implicity_annotation() throws Exception {
        def library = library().name('test_cross_class_tres')
                        .defaultVersion("gamma")
                        .allowOverride(false)
                        .implicit(false)
                        .targetPath(sharedLibTwo)
                        .retriever(projectSource(sharedLibTwo))
                        .build()
        helper.registerSharedLibrary(library)

        runScript('job/library/cross_class/test_annotation.jenkins')
        printCallStack()
        assertCallStack().contains("""ClassA.methodA()""")
        assertCallStack().contains("""ClassA.sh(echo 'ClassA: I'm field of A')""")
        assertCallStack().contains("""ClassB.methodB()""")
        assertCallStack().contains("""ClassB.sh(echo 'ClassB: I'm field of B')""")
    }

    @Test
    void test_cross_lib_interop_library_loaded_with_implicity() throws Exception {
        def libraryA = library().name('test_cross_class_uno')
                        .defaultVersion("alpha")
                        .allowOverride(false)
                        .implicit(true)
                        .targetPath(sharedLibTwo)
                        .retriever(projectSource(sharedLibTwo))
                        .build()
        helper.registerSharedLibrary(libraryA)
        def libraryB = library().name('test_cross_class_uno')
                        .defaultVersion("alpha")
                        .allowOverride(false)
                        .implicit(true)
                        .targetPath(sharedLibTwo)
                        .retriever(projectSource(sharedLibTwo))
                        .build()
        helper.registerSharedLibrary(libraryA)

        runScript('job/library/cross_class/test_implicity.jenkins')
        printCallStack()
        assertCallStack().contains("""ClassA.methodA()""")
        assertCallStack().contains("""ClassA.sh(echo 'ClassA: I'm field of A')""")
        assertCallStack().contains("""ClassB.methodB()""")
        assertCallStack().contains("""ClassB.sh(echo 'ClassB: I'm field of B')""")
    }

    @Test
    void test_cross_lib_interop_no_implicity_dynamic() throws Exception {
        def library = library().name('test_cross_class_dos')
                        .defaultVersion("beta")
                        .allowOverride(false)
                        .implicit(false)
                        .targetPath(sharedLibTwo)
                        .retriever(projectSource(sharedLibTwo))
                        .build()
        helper.registerSharedLibrary(library)

        runScript('job/library/cross_class/test_dynamic.jenkins')
        printCallStack()
        assertCallStack().contains("""ClassA.methodA()""")
        assertCallStack().contains("""ClassA.sh(echo 'ClassA: I'm field of A')""")
        assertCallStack().contains("""ClassB.methodB()""")
        assertCallStack().contains("""ClassB.sh(echo 'ClassB: I'm field of B')""")
    }

    @Test
    void test_cross_lib_interop_no_implicity_annotation() throws Exception {
        def library = library().name('test_cross_class_tres')
                        .defaultVersion("gamma")
                        .allowOverride(false)
                        .implicit(false)
                        .targetPath(sharedLibTwo)
                        .retriever(projectSource(sharedLibTwo))
                        .build()
        helper.registerSharedLibrary(library)

        runScript('job/library/cross_class/test_annotation.jenkins')
        printCallStack()
        assertCallStack().contains("""ClassA.methodA()""")
        assertCallStack().contains("""ClassA.sh(echo 'ClassA: I'm field of A')""")
        assertCallStack().contains("""ClassB.methodB()""")
        assertCallStack().contains("""ClassB.sh(echo 'ClassB: I'm field of B')""")
    }

}
