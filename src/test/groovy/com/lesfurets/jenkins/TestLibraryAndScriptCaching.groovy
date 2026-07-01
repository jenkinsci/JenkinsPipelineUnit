package com.lesfurets.jenkins

import com.lesfurets.jenkins.unit.BasePipelineTest
import com.lesfurets.jenkins.unit.global.lib.LibraryLoader
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

import static com.lesfurets.jenkins.unit.global.lib.LibraryConfiguration.library
import static com.lesfurets.jenkins.unit.global.lib.ProjectSource.projectSource
import static org.junit.jupiter.api.Assertions.assertEquals
import static org.junit.jupiter.api.Assertions.assertNotEquals

class TestLibraryAndScriptCaching extends BasePipelineTest {

    @Override
    @BeforeEach
    void setUp() throws Exception {
        scriptRoots += 'src/test/jenkins'
        super.setUp()
    }

    private final library = library().name('commons')
            .defaultVersion('<notNeeded>')
            .allowOverride(false)
            .implicit(false)
            .targetPath('<notNeeded>')
            .retriever(projectSource(this.class.getResource('/libs/commons@master').getFile()))
            .build()

    @Test
    void scriptCacheDisabled() {
        def script1 = loadScript('job/exampleJob.jenkins')
        helper.init()
        def script2 = loadScript('job/exampleJob.jenkins')
        assertNotEquals(script1.metaClass.theClass, script2.metaClass.theClass)
    }

    @Test
    void scriptCacheEnabledDisabled() {
        helper.withShouldCacheScriptsAndLibraries(true)
        def script1 = loadScript('job/exampleJob.jenkins')
        helper.init()
        def script2 = loadScript('job/exampleJob.jenkins')
        assertEquals(script1.metaClass.theClass, script2.metaClass.theClass)
    }

    @Test
    void libraryCacheDisabled() {
        helper.registerSharedLibrary(library)
        helper.libLoader.loadLibrary(library.name)
        def lib1 = LibraryLoader.libRecords.get("commons@<notNeeded>")
        helper.init()
        helper.registerSharedLibrary(library)
        helper.libLoader.loadLibrary(library.name)
        def lib2 = LibraryLoader.libRecords.get("commons@<notNeeded>")
        assertNotEquals(lib1, lib2)
    }

    @Test
    void libraryCacheEnabled() {
        helper.withShouldCacheScriptsAndLibraries(true)
        helper.registerSharedLibrary(library)
        helper.libLoader.loadLibrary(library.name)
        def lib1 = LibraryLoader.libRecords.get("commons@<notNeeded>")
        helper.init()
        helper.registerSharedLibrary(library)
        helper.libLoader.loadLibrary(library.name)
        def lib2 = LibraryLoader.libRecords.get("commons@<notNeeded>")
        assertEquals(lib1, lib2)
    }
}
