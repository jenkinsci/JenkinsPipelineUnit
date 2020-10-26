package com.lesfurets.jenkins

import org.junit.Before
import org.junit.Test
import org.junit.Rule
import org.junit.rules.ExpectedException

import com.lesfurets.jenkins.unit.cps.BasePipelineTestCPS
import static com.lesfurets.jenkins.unit.global.lib.LibraryConfiguration.library
import static com.lesfurets.jenkins.unit.global.lib.ProjectSource.projectSource

class TestNotSerrialzibleCPS extends BasePipelineTestCPS {
    @Rule
    public ExpectedException thrown = ExpectedException.none();

    String sharedLibs = this.class
        .getResource('/libs/test_not_serrializable_cps')
        .getFile()

    @Override
    @Before
    void setUp() throws Exception {
        helper.registerSharedLibrary(library()
            .name('test_not_serrializable_cps')
            .allowOverride(false)
            .retriever(projectSource(sharedLibs))
            .targetPath(sharedLibs)
            .defaultVersion('master')
            .implicit(true)
            .build()
        )
        scriptRoots += 'src/test/jenkins'
        super.setUp()

    }

    @Test
    void default_run() {
        thrown.expect(Exception.class)
        thrown.expectMessage(containsString("Unable to serialize locals"))
        def script = loadScript('job/testNotSerrializableCPS.jenkins')
        script.execute()
        printCallStack()
    }


}
