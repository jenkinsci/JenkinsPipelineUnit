package com.lesfurets.jenkins

import com.lesfurets.jenkins.unit.cps.BasePipelineTestCPS
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.Parameter
import org.junit.jupiter.params.ParameterizedClass
import org.junit.jupiter.params.provider.MethodSource

import static com.lesfurets.jenkins.unit.global.lib.LibraryConfiguration.library
import static com.lesfurets.jenkins.unit.global.lib.ProjectSource.projectSource
import static org.assertj.core.api.Assertions.assertThat

@ParameterizedClass(name = "Test {0} allowOverride:{1} implicit:{2} expected:{3}")
@MethodSource("data")
class TestSharedLibraryWithProjectSourceRetrieverCPS extends BasePipelineTestCPS {

    // For simplicity we use the common@master here. In this case 'commons@master'
    // does not denote the master branch of the commons lib. In this case it is
    // just a folder containing the lib. ProjectSourceRetriever is by design
    // agnostic to branches and agnostic to library names.
    // By default ProjectSourceRetriever works upon '.', which is the project
    // root directory. Here for testing the retriever is used in a way so that
    // it refers to the folder mentioned below.
    String sharedLibs = this.class.getResource('/libs/commons@master').getFile()

    @Parameter(0)
    public String script
    @Parameter(1)
    public boolean allowOverride
    @Parameter(2)
    public boolean implicit
    @Parameter(3)
    public boolean expected

    @Override
    @BeforeEach
    void setUp() throws Exception {
        scriptRoots += 'src/test/jenkins'
        super.setUp()
    }

    static Collection<Object[]> data() {
        return [['libraryJob', false, false, false],
         ['libraryJob_implicit', false, false, true],
         ['libraryJob_implicit', false, true, false],

         // Utils2, denoted in the job below, cannot be resolved
         // since we do not have the lib from the feature branch
         // despite this is requested in the pipeline.
         // ProjectSourceRetriever does by design
         // not take branches into account.
         ['libraryJob_feature', true, true, true],

        ].collect { it as Object[] }
    }

    @Test
    void library_annotation() throws Exception {
        boolean exception = false
        def library = library().name('commons')
                        .defaultVersion('<notNeeded>')
                        .allowOverride(allowOverride)
                        .implicit(implicit)
                        .targetPath('<notNeeded>')
                        .retriever(projectSource(sharedLibs))
                        .build()
        helper.registerSharedLibrary(library)
        try {
            def script = runScript("job/library/${script}.jenkins")
            script.execute()
            printCallStack()
        } catch (e) {
            e.printStackTrace()
            exception = true
        }
        assertThat(expected).isEqualTo(exception)

    }
}
