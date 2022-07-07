package com.lesfurets.jenkins

import com.lesfurets.jenkins.unit.declarative.DeclarativePipelineTest
import org.junit.Assert
import org.junit.Before
import org.junit.Test

import static com.lesfurets.jenkins.unit.global.lib.LibraryConfiguration.library
import static com.lesfurets.jenkins.unit.global.lib.ProjectSource.projectSource

class TestSharedLibraryEnvVariable extends DeclarativePipelineTest {

    String sharedLibVars = this.class.getResource("/libs/env_var_not_defined").getFile()

    @Override
    @Before
    void setUp() throws Exception {
        scriptRoots += 'src/test/jenkins'

        super.setUp()

        def library = library().name("env_var_not_defined")
                .retriever(projectSource(sharedLibVars))
                .defaultVersion("master")
                .targetPath(sharedLibVars)
                .allowOverride(true)
                .implicit(false)
                .build()
        helper.registerSharedLibrary(library)
    }

    @Test
    void "test lib var not defined in env"() {

        runScript("job/library/test_lib_var_not_defined_in_env.jenkins")

        def prop1 = binding.env["prop1"]
        Assert.assertEquals("magic", prop1)
    }

    @Test
    void "test params not defined in env"() {
        def version =  "1.2.0"
        addParam("VERSION", version)

        runScript("job/library/test_params_not_defined_in_env.jenkins")

        def versionFromEnv = binding.env["VERSION"]
        Assert.assertEquals(version, versionFromEnv.toString())
    }
}
