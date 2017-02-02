package com.lesfurets.jenkins

import static org.assertj.core.api.Assertions.assertThat

import java.nio.charset.Charset

import org.assertj.core.util.Files
import org.junit.Before
import org.junit.Test

import com.lesfurets.jenkins.helpers.BasePipelineTest
import static com.lesfurets.jenkins.helpers.MethodSignature.method

class TestUtilsLib extends BasePipelineTest {

    @Override
    @Before
    void setUp() throws Exception {
        super.setUp()
    }

    @Test
    void should_return_name_with_uppercase_underscore() throws Exception {
        Script commons = loadScript("lib/utils.jenkins")
        assertThat(commons.cleanName('some name')).isEqualTo("SOME_NAME")
        assertThat(helper.methodCallCount("cleanName")).isEqualTo(1)
        printCallStack()
    }


    @Test
    void testVersionForPom() throws Exception {
        // given
        helper.registerAllowedMethod(method("readFile", String.class), { file ->
            return Files.contentOf(new File(file), Charset.forName("UTF-8"))
        })
        // when
        Script commons = helper.loadScript("lib/utils.jenkins")
        def pomVersion = commons.versionForPom('src/test/resources/test-pom.xml', 'lesfurets.version')
        // then
        assertThat(pomVersion).isEqualTo("5.14.9")
    }

    @Test
    void testCurrentRevision() {
        // given
        Script commons = loadScript("lib/utils.jenkins")
        helper.registerAllowedMethod(method('sh', Map.class), { map ->
            if (map.script == 'git rev-parse HEAD') {
                return '29480a51e10718600371e8465e631cec34aa9ab3'
            }
            return "0"
        })
        // when
        String result = commons.currentRevision()
        // then
        assertThat(result).isEqualTo('29480a51e10718600371e8465e631cec34aa9ab3')

    }

    @Test
    void testRepositoryBranchSplitRefsRemotes() throws Exception {
        Script commons = helper.loadScript("lib/utils.jenkins", new Binding())

        String[] branchs = ["refs/remotes/origin/AMX-11701_pc_cleanup_garanties ",
                            "refs/remotes/origin/AMX-12378_responsive_SEA_pages ",
                            "refs/remotes/origin/AMX-12480_refactoring_backends_cleanups_renames",]

        def branchSplit = commons.getRepositoryBranchSplit(branchs, "origin")

        assertThat(branchSplit).containsExactly(
                        ["origin", "AMX-11701_pc_cleanup_garanties"] as String[],
                        ["origin", "AMX-12378_responsive_SEA_pages"] as String[],
                        ["origin", "AMX-12480_refactoring_backends_cleanups_renames"] as String[],
        )
    }

}
