package com.lesfurets.jenkins

import static com.lesfurets.jenkins.unit.global.lib.LocalSource.localSource
import static com.lesfurets.jenkins.unit.global.lib.LibraryConfiguration.library

import org.assertj.core.api.Assertions
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import org.junit.runners.Parameterized.Parameter
import org.junit.runners.Parameterized.Parameters

import com.lesfurets.jenkins.unit.cps.BasePipelineTestCPS

@RunWith(Parameterized.class)
class TestSharedLibrary extends BasePipelineTestCPS {

    @Rule
    public TemporaryFolder folder = new TemporaryFolder()

    String shared_scripts = this.class.getResource('/shared-scripts').getFile()

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
        baseClassLoader = URLClassLoader.newInstance()
        super.setUp()
    }

    @Parameters(name = "Test {0} allowOverride:{1} implicit:{2} expected:{3}")
    static Collection<Object[]> data() {
        return [['libraryJob', false, false, false],
         ['libraryJob', false, false, false],
         ['libraryJob_implicit', false, false, true],
         ['libraryJob_implicit', false, true, false],
         ['libraryJob_master', true, false, false],
         ['libraryJob_master', false, false, false],
         ['libraryJob_feature', true, false, false],
         ['libraryJob_feature', false, false, true],
         ['libraryJob_feature2', true, false, true],
        ].collect { it as Object[] }
    }

    @Test
    void library_annotation() throws Exception {
        boolean exception = false
        def library = library().name('commons')
                        .defaultVersion("master")
                        .allowOverride(allowOverride)
                        .implicit(implicit)
                        .retriever(localSource().sourceURL(shared_scripts).build())
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
        Assertions.assertThat(expected).isEqualTo(exception)

    }
}
