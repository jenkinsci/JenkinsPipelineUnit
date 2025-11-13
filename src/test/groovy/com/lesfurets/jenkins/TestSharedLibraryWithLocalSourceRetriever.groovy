package com.lesfurets.jenkins

import com.lesfurets.jenkins.unit.BasePipelineTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import org.junit.jupiter.params.Parameter
import org.junit.jupiter.params.ParameterizedClass
import org.junit.jupiter.params.provider.MethodSource

import static com.lesfurets.jenkins.unit.global.lib.LibraryConfiguration.library
import static com.lesfurets.jenkins.unit.global.lib.LocalSource.localSource
import static org.assertj.core.api.Assertions.assertThat

@ParameterizedClass(name = "Test {0} allowOverride:{1} implicit:{2} expected:{3}")
@MethodSource("data")
class TestSharedLibraryWithLocalSourceRetriever extends BasePipelineTest {

    @TempDir
    public File folder

    String sharedLibs = this.class.getResource('/libs').getFile()

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
        binding.setVariable('scm', [branch: 'master'])
    }

    static Collection<Object[]> data() {
        return [['libraryJob', false, false, false],
         ['libraryJob_implicit', false, false, true],
         ['libraryJob_implicit', false, true, false],
         ['libraryJob_master', true, false, false],
         ['libraryJob_master', false, false, false],
         ['libraryJob_feature', true, false, false],
         ['libraryJob_feature', false, false, true],
         ['libraryJob_feature2', true, false, true],
         ['libraryJob_inline_library', false, false, true],
         ['libraryJob_dynamic_map', false, false, false],
        ].collect { it as Object[] }
    }

    @Test
    void library_annotation() throws Exception {
        boolean exception = false
        def library = library().name('commons')
                        .defaultVersion("master")
                        .allowOverride(allowOverride)
                        .implicit(implicit)
                        .targetPath(sharedLibs)
                        .retriever(localSource(sharedLibs))
                        .build()
        helper.registerSharedLibrary(library)
        try {
            def script = loadScript("job/library/${script}.jenkins")
            script.execute()
            printCallStack()
        } catch (e) {
            e.printStackTrace()
            exception = true
        }
        assertThat(expected).isEqualTo(exception)

    }
}
