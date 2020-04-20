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
    String sharedLibVars = this.class
        .getResource('/libs/test_cross_vars_usage')
        .getFile()

    String sharedLibCls = this.class
        .getResource('/libs/test_cross_class_usage')
        .getFile()

    /**
    * 1. Load library dinamicaly, set implicity=true
    * 2. Call vars/methodAB.groovy
    * should be able to call vars/methodA.groovy and
    * vars/methodB.groovy
    */
    @Test
    void test_vars_interop_library_loaded_with_implicity() throws Exception {
        def library = library().name('test_cross_vars_uno')
                        .defaultVersion("alpha")
                        .allowOverride(false)
                        .implicit(true)
                        .targetPath(sharedLibVars)
                        .retriever(projectSource(sharedLibVars))
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

    /**
    * 1. Load library dinamicaly, without implicity
    * 2. Call vars/methodAB.groovy
    * should be able to call vars/methodA.groovy and
    * vars/methodB.groovy
    */
    @Test
    void test_vars_interop_no_implicity_dynamic() throws Exception {
        def library = library().name('test_cross_vars_dos')
                        .defaultVersion("beta")
                        .allowOverride(false)
                        .implicit(false)
                        .targetPath(sharedLibVars)
                        .retriever(projectSource(sharedLibVars))
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

    /**
    * 1. Load library using @Library annotation, without implicity
    * 2. Call vars/methodAB.groovy
    * should be able to call vars/methodA.groovy and
    * vars/methodB.groovy
    */
    @Test
    void test_vars_interop_no_implicity_annotation() throws Exception {
        def library = library().name('test_cross_vars_tres')
                        .defaultVersion("gamma")
                        .allowOverride(false)
                        .implicit(false)
                        .targetPath(sharedLibVars)
                        .retriever(projectSource(sharedLibVars))
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

    /**
    * 1. Load library dinamicaly, without implicity
    * 2. Create an instace of a library class
    * 3. The library class method shoud be able to call pipeline methods
    *    e.g. someMethod() {sh "whoami"}
    */
    @Test
    void test_lib_call_shell_no_implicity_dynamic() throws Exception {
        def library = library().name('test_lib_call_shell_uno')
                        .defaultVersion("alpha")
                        .allowOverride(false)
                        .implicit(false)
                        .targetPath(sharedLibVars)
                        .retriever(projectSource(sharedLibVars))
                        .build()
        helper.registerSharedLibrary(library)
        runScript('job/library/test_lib_call_shell_dynamic.jenkins')
        printCallStack()
        assertCallStack().contains("""LaClass.sh(echo 'run ls -la with bash')""")
    }
    /**
    * 1. Load library unsing @library annotation, without implicity
    * 2. Create an instace of a library class
    * 3. The library class method shoud be able to call pipeline methods
    *    e.g. someMethod() {sh "whoami"}
    */
    @Test
    void test_lib_call_shell_no_implicity_annotation() throws Exception {
        def library = library().name('test_lib_call_shell_dos')
                        .defaultVersion("beta")
                        .allowOverride(false)
                        .implicit(false)
                        .targetPath(sharedLibVars)
                        .retriever(projectSource(sharedLibVars))
                        .build()
        helper.registerSharedLibrary(library)
        runScript('job/library/test_lib_call_shell_annotation.jenkins')
        printCallStack()
        assertCallStack().contains("""LaClass.sh(echo 'run ls -la with bash')""")
    }

    /**
    * 1. Load library dinamicaly, set implicity=true
    * 2. Create instaces of a library class
    * 3. Make sure that within a class method you can create an object of another class
    * 4. Make sure you can call methods of such objects
    * 5. Make sure interceprion of missing methods of pipeline works propertly
    */
    @Test
    void test_cross_class_interop_library_loaded_with_implicity() throws Exception {
        def library = library().name('test_cross_class_uno')
                        .defaultVersion("alpha")
                        .allowOverride(false)
                        .implicit(true)
                        .targetPath(sharedLibCls)
                        .retriever(projectSource(sharedLibCls))
                        .build()
        helper.registerSharedLibrary(library)

        runScript('job/library/cross_class/test_implicity.jenkins')
        printCallStack()
        assertCallStack().contains("""ClassAB.methodAB()""")
        assertCallStack().contains("""ClassA.methodA()""")
        assertCallStack().contains("""ClassA.sh(echo 'ClassA: I'm field of A')""")
        assertCallStack().contains("""ClassB.methodB()""")
        assertCallStack().contains("""ClassB.sh(echo 'ClassB: I'm field of B')""")
    }

    /**
    * 1. Load library dinamicaly, without implicity
    * 2. Create instace of a library class
    * 3. Make sure that within a class method you can create an object of another library class
    * 4. Make sure you can call methods of such objects
    * 5. Make sure interceprion of pipeline methods works propertly
    */
    @Test
    void test_cross_class_interop_no_implicity_dynamic() throws Exception {
        def library = library().name('test_cross_class_dos')
                        .defaultVersion("beta")
                        .allowOverride(false)
                        .implicit(false)
                        .targetPath(sharedLibCls)
                        .retriever(projectSource(sharedLibCls))
                        .build()
        helper.registerSharedLibrary(library)

        runScript('job/library/cross_class/test_dynamic.jenkins')
        printCallStack()
        assertCallStack().contains("""ClassA.methodA()""")
        assertCallStack().contains("""ClassA.sh(echo 'ClassA: I'm field of A')""")
        assertCallStack().contains("""ClassB.methodB()""")
        assertCallStack().contains("""ClassB.sh(echo 'ClassB: I'm field of B')""")
    }

    /**
    * 1. Load library by annotation, without implicity
    * 2. Create instaces of library class
    * 3. Make sure that within a class method you can create an object of another class
    * 4. Make sure you can call methods of such objects
    * 5. Make sure interceprion of pipeline methods works propertly
    */
    @Test
    void test_cross_class_interop_no_implicity_annotation() throws Exception {
        def library = library().name('test_cross_class_tres')
                        .defaultVersion("gamma")
                        .allowOverride(false)
                        .implicit(false)
                        .targetPath(sharedLibCls)
                        .retriever(projectSource(sharedLibCls))
                        .build()
        helper.registerSharedLibrary(library)

        runScript('job/library/cross_class/test_annotation.jenkins')
        printCallStack()
        assertCallStack().contains("""ClassA.methodA()""")
        assertCallStack().contains("""ClassA.sh(echo 'ClassA: I'm field of A')""")
        assertCallStack().contains("""ClassB.methodB()""")
        assertCallStack().contains("""ClassB.sh(echo 'ClassB: I'm field of B')""")
    }

    /**
    * 1. Load library dinamicaly, set implicity=true
    * 2. Create instances of all library classes
    * 3. Make sure that within a class method you can create an object of another class
    * 4. Make sure you can call methods of such objects
    * 5. Make sure interceprion of missing methods of pipeline works propertly
    */
    @Test
    void test_pre_loaded_cross_class_interop_library_loaded_with_implicity() throws Exception {
        def library = library().name('test_cross_class_uno')
                        .defaultVersion("alpha")
                        .allowOverride(false)
                        .implicit(true)
                        .targetPath(sharedLibCls)
                        .retriever(projectSource(sharedLibCls))
                        .build()
        helper.registerSharedLibrary(library)

        runScript('job/library/cross_class_pre_loaded/test_implicity.jenkins')
        printCallStack()
        assertCallStack().contains("""ClassAB.methodAB()""")
        assertCallStack().contains("""ClassA.methodA()""")
        assertCallStack().contains("""ClassA.sh(echo 'ClassA: I'm field of A')""")
        assertCallStack().contains("""ClassB.methodB()""")
        assertCallStack().contains("""ClassB.sh(echo 'ClassB: I'm field of B')""")
    }

    /**
    * 1. Load library dinamicaly, without implicity
    * 2. Create instace of all library classes
    * 3. Make sure that within a class method you can create an object of another library class
    * 4. Make sure you can call methods of such objects
    * 5. Make sure interceprion of pipeline methods works propertly
    */
    @Test
    void test_pre_loaded_cross_class_interop_no_implicity_dynamic() throws Exception {
        def library = library().name('test_cross_class_dos')
                        .defaultVersion("beta")
                        .allowOverride(false)
                        .implicit(false)
                        .targetPath(sharedLibCls)
                        .retriever(projectSource(sharedLibCls))
                        .build()
        helper.registerSharedLibrary(library)

        runScript('job/library/cross_class_pre_loaded/test_dynamic.jenkins')
        printCallStack()
        assertCallStack().contains("""ClassA.methodA()""")
        assertCallStack().contains("""ClassA.sh(echo 'ClassA: I'm field of A')""")
        assertCallStack().contains("""ClassB.methodB()""")
        assertCallStack().contains("""ClassB.sh(echo 'ClassB: I'm field of B')""")
    }

    /**
    * 1. Load library by annotation, without implicity
    * 2. Create instaces of library classes
    * 3. Make sure that within a class method you can create an object of another class
    * 4. Make sure you can call methods of such objects
    * 5. Make sure interceprion of pipeline methods works propertly
    */
    @Test
    void test_pre_loaded_cross_class_interop_no_implicity_annotation() throws Exception {
        def library = library().name('test_cross_class_tres')
                        .defaultVersion("gamma")
                        .allowOverride(false)
                        .implicit(false)
                        .targetPath(sharedLibCls)
                        .retriever(projectSource(sharedLibCls))
                        .build()
        helper.registerSharedLibrary(library)

        runScript('job/library/cross_class_pre_loaded/test_annotation.jenkins')
        printCallStack()
        assertCallStack().contains("""ClassA.methodA()""")
        assertCallStack().contains("""ClassA.sh(echo 'ClassA: I'm field of A')""")
        assertCallStack().contains("""ClassB.methodB()""")
        assertCallStack().contains("""ClassB.sh(echo 'ClassB: I'm field of B')""")
    }

    /**
    * 1. Load library dinamicaly, set implicity=true
    * 2. Create instaces of library classes passing there the reference to the script object
    * 3. Make sure that within a class method you can create an object of another class
    * 4. Make sure you can call methods of such objects
    * 5. Make sure interceprion of missing methods of pipeline works propertly
    */
    @Test
    void test_cross_class_with_pipeline_ref_interop_library_loaded_with_implicity() throws Exception {
        def library = library().name('test_cross_class_with_pipeline_ref_uno')
                        .defaultVersion("alpha")
                        .allowOverride(false)
                        .implicit(true)
                        .targetPath(sharedLibCls)
                        .retriever(projectSource(sharedLibCls))
                        .build()
        helper.registerSharedLibrary(library)

        runScript('job/library/cross_class_with_pipeline_ref/test_implicity.jenkins')
        printCallStack()
        assertCallStack().contains("""ClassAB.methodAB()""")
        assertCallStack().contains("""test_implicity.sh(echo 'ClassA: I'm field of A')""")
        assertCallStack().contains("""test_implicity.sh(echo 'ClassB: I'm field of B')""")
    }

    /**
    * 1. Load library dinamicaly, without implicity
    * 2. Create instaces of library classes passing there the reference to the script object
    * 3. Make sure that within a class method you can create an object of another library class
    * 4. Make sure you can call methods of such objects
    * 5. Make sure interceprion of pipeline methods works propertly
    */
    @Test
    void test_cross_class_with_pipeline_ref_interop_no_implicity_dynamic() throws Exception {
        def library = library().name('test_cross_class_with_pipeline_ref_dos')
                        .defaultVersion("beta")
                        .allowOverride(false)
                        .implicit(false)
                        .targetPath(sharedLibCls)
                        .retriever(projectSource(sharedLibCls))
                        .build()
        helper.registerSharedLibrary(library)

        runScript('job/library/cross_class_with_pipeline_ref/test_dynamic.jenkins')
        printCallStack()
        assertCallStack().contains("""ClassAB.methodAB()""")
        assertCallStack().contains("""test_dynamic.sh(echo 'ClassA: I'm field of A')""")
        assertCallStack().contains("""test_dynamic.sh(echo 'ClassB: I'm field of B')""")
    }

    /**
    * 1. Load library by annotation, without implicity
    * 2. Create instaces of library classes passing there the reference to the script object
    * 3. Make sure that within a class method you can create an object of another class
    * 4. Make sure you can call methods of such objects
    * 5. Make sure interceprion of pipeline methods works propertly
    */
    @Test
    void test_cross_class_with_pipeline_ref_interop_no_implicity_annotation() throws Exception {
        def library = library().name('test_cross_class_with_pipeline_ref_tres')
                        .defaultVersion("gamma")
                        .allowOverride(false)
                        .implicit(false)
                        .targetPath(sharedLibCls)
                        .retriever(projectSource(sharedLibCls))
                        .build()
        helper.registerSharedLibrary(library)

        runScript('job/library/cross_class_with_pipeline_ref/test_annotation.jenkins')
        printCallStack()
        assertCallStack().contains("""ClassAB.methodAB()""")
        assertCallStack().contains("""test_annotation.sh(echo 'ClassA: I'm field of A')""")
        assertCallStack().contains("""test_annotation.sh(echo 'ClassB: I'm field of B')""")
    }


}
