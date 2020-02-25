import org.junit.Before
import org.junit.Test

import static com.lesfurets.jenkins.unit.global.lib.LibraryConfiguration.library
import static com.lesfurets.jenkins.unit.global.lib.LocalSource.localSource

import static org.assertj.core.api.Assertions.assertThat

import org.example.testsharedlib.ModBasePipelineTestCPS

class CPS2Test extends ModBasePipelineTestCPS {
  @Override
  @Before
  void setUp() throws Exception {
    String sharedLibs = this.class.getResource('.').getFile()

    helper.registerSharedLibrary(library()
        .name('testsharedlib')
        .allowOverride(false)
        .retriever(localSource(sharedLibs))
        .targetPath(sharedLibs)
        .defaultVersion('snapshot')
        .implicit(true)
        .build()
    )

    setScriptRoots([ 'jobs' ] as String[])
    setScriptExtension('groovy')

    super.setUp()
  }

  @Test
  void default_run() throws Exception {
    def script = loadScript('exampleJob.jenkins')
    script.execute()
    printCallStack()
  }
}
