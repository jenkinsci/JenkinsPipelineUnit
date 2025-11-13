package com.lesfurets.jenkins.unit.declarative

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class TestDeclarativeWithCredentials extends DeclarativePipelineTest {

    @Override
    @BeforeEach
    void setUp() throws Exception {
        scriptRoots += 'src/test/jenkins/jenkinsfiles'
        scriptExtension = ''
        super.setUp()
    }

    @Test
    void should_run_script_with_credentials() {
        // when:
        runScript("withCredentials_Jenkinsfile")

        // then:
        assertJobStatusSuccess()
        assertCallStack().contains("echo(User/Pass = user/pass)")
        assertCallStack().contains("echo(Nested User/Pass = user/pass)")
        assertCallStack().contains("echo(Restored User/Pass = user/pass)")
        assertCallStack().contains("echo(Cleared User/Pass = null/null)")

        assertCallStack().contains("echo(Docker = docker_pass)")
        assertCallStack().contains("echo(Nested Docker = docker_pass)")
        assertCallStack().contains("echo(Restored Docker = docker_pass)")
        assertCallStack().contains("echo(Cleared Docker = null)")

        assertCallStack().contains("echo(SSH = ssh_pass)")
        assertCallStack().contains("echo(Nested SSH = ssh_pass)")
        assertCallStack().contains("echo(Restored SSH = ssh_pass)")
        assertCallStack().contains("echo(Cleared SSH = null)")
    }
}
