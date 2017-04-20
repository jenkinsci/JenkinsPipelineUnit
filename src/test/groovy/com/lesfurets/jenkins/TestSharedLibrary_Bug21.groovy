package com.lesfurets.jenkins

import com.lesfurets.jenkins.unit.BasePipelineTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import org.junit.runners.Parameterized.Parameter
import org.junit.runners.Parameterized.Parameters

import static com.lesfurets.jenkins.unit.global.lib.LibraryConfiguration.library
import static com.lesfurets.jenkins.unit.global.lib.LocalSource.localSource
import static org.assertj.core.api.Assertions.assertThat

@RunWith(Parameterized.class)
class TestSharedLibrary_Bug21 extends BasePipelineTest {

    @Rule
    public TemporaryFolder folder = new TemporaryFolder()

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
    @Before
    void setUp() throws Exception {
        scriptRoots += 'src/test/jenkins'
        super.setUp()
        binding.setVariable('scm', [branch: 'master'])
    }

    @Parameters(name = "Test {0} allowOverride:{1} implicit:{2} expected:{3}")
    static Collection<Object[]> data() {
        return [['libraryJob_bug21', false, false, false]
        ].collect { it as Object[] }
    }

    @Test
    void library_annotation() throws Exception {
        boolean exception = false
        println "Library: ${sharedLibs}"
        println "Script: job/library/${script}.jenkins"
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
