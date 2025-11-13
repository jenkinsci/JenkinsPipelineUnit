package com.lesfurets.jenkins

import com.lesfurets.jenkins.unit.BasePipelineTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

import static com.lesfurets.jenkins.unit.global.lib.LibraryConfiguration.library
import static com.lesfurets.jenkins.unit.global.lib.LocalSource.localSource

class TestInlineScript extends BasePipelineTest {

    String sharedLibs = this.class.getResource('/libs').getFile()

    @Override
    @BeforeEach
    void setUp() throws Exception {
        scriptRoots += 'src/test/jenkins'
        super.setUp()

        def library = library()
                        .name('commons')
                        .defaultVersion('master')
                        .allowOverride(true)
                        .implicit(false)
                        .targetPath(sharedLibs)
                        .retriever(localSource(sharedLibs))
                        .build()

        helper.registerSharedLibrary(library)
    }

    @Test
    void load_inline_script_with_simple_commands() {
        def script = loadInlineScript('''
            node {
                echo 'Test'
            }
        ''')

        script.run()

        printCallStack()
        assertJobStatusSuccess()
    }

    @Test
    void run_inline_script_with_simple_commands() {
        runInlineScript('''
            node {
                echo 'Test'
            }
        ''')

        printCallStack()
        assertJobStatusSuccess()
    }

    @Test
    void load_inline_script_with_shared_library() {
        def script = loadInlineScript('''
            @Library('commons') _

            node {
                sayHello()
            }
        ''')

        script.run()

        printCallStack()
        assertJobStatusSuccess()
    }

    @Test
    void run_inline_script_with_shared_library() {
        runInlineScript('''
            @Library('commons') _

            node {
                sayHello()
            }
        ''')

        printCallStack()
        assertJobStatusSuccess()
    }
}
