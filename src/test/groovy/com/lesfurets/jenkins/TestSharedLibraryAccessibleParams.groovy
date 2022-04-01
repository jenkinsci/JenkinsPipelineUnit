package com.lesfurets.jenkins

import com.lesfurets.jenkins.unit.declarative.DeclarativePipelineTest
import org.junit.Before
import org.junit.Test

import static com.lesfurets.jenkins.unit.global.lib.LibraryConfiguration.library
import static com.lesfurets.jenkins.unit.global.lib.ProjectSource.projectSource

class TestSharedLibraryAccessibleParams extends DeclarativePipelineTest {

    private final String JOB_NAME = "params_not_accessible"
    private final String JOB_PATH = "job/library/${JOB_NAME}.jenkins"
    private final String LIB_DIR = this.class.getResource("/libs/$JOB_NAME").getFile()

    @Override
    @Before
    void setUp() throws Exception {
        scriptRoots += 'src/test/jenkins'
        super.setUp()
        def library = library().name(JOB_NAME)
                .retriever(projectSource(LIB_DIR))
                .defaultVersion("master")
                .targetPath(LIB_DIR)
                .allowOverride(true)
                .implicit(false)
                .build()
        helper.registerSharedLibrary(library)
    }

    @Test
    void accessible_params_test() {
        binding.setVariable('testVar', 'notBroken')
        runScript(JOB_PATH)
        assertJobStatusSuccess()
    }

    @Test(expected = MissingPropertyException.class)
    void not_accessible_params_test() {
        runScript(JOB_PATH)
    }
}
