package com.lesfurets.jenkins

import com.lesfurets.jenkins.unit.declarative.DeclarativePipelineTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

import static com.lesfurets.jenkins.unit.global.lib.LibraryConfiguration.library
import static com.lesfurets.jenkins.unit.global.lib.ProjectSource.projectSource
import static org.assertj.core.api.Assertions.assertThat
import static org.junit.jupiter.api.Assertions.assertThrows

class TestSharedLibraryAccessibleParams extends DeclarativePipelineTest {

    private final String JOB_NAME = "params_not_accessible"
    private final String JOB_PATH = "job/library/${JOB_NAME}.jenkins"
    private final String LIB_DIR = this.class.getResource("/libs/$JOB_NAME").getFile()
    private final String BINDING_VAR = "testVar"
    private final String BINDING_VAL = "notBroken"

    @Override
    @BeforeEach
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
        run_test_with_bindings {assertJobStatusSuccess()}
    }

    @Test
    void change_binding_test() {
        run_test_with_bindings {assertThat(binding.getVariable(BINDING_VAR)).isNotEqualTo(BINDING_VAL)}
    }

    @Test
    void not_accessible_params_test() {
        assertThrows(MissingPropertyException.class, { ->
            runScript(JOB_PATH)
        })
    }

    private void run_test_with_bindings(Closure assertion) {
        binding.setVariable(BINDING_VAR, BINDING_VAL)
        runScript(JOB_PATH)
        assertion()
    }

}
