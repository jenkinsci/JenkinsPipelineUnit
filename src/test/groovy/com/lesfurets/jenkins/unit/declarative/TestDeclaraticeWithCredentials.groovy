package com.lesfurets.jenkins.unit.declarative

import com.lesfurets.jenkins.unit.declarative.DeclarativePipelineTest
import org.junit.Before
import org.junit.Test

import static org.junit.Assert.assertTrue

class TestDeclaraticeWithCredentials extends DeclarativePipelineTest {

    @Override
    @Before
    void setUp() throws Exception {
        scriptRoots += 'src/test/jenkins/jenkinsfiles'
        scriptExtension = ''
        super.setUp()
    }

    @Test
    void should_run_script_with_credentials() {
        // given:
        addUsernamePasswordCredential('my_cred_id-1', 'test-user-1', 'test-password-1')
        addUsernamePasswordCredential('my_cred_id-2', 'test-user-2', 'test-password-2')
        addStringCredential('docker_cred-1', 'docker-pass-1')
        addStringCredential('docker_cred-2', 'docker-pass-2')
        addStringCredential('ssh_cred-1', 'ssh-pass-1')
        addStringCredential('ssh_cred-2', 'ssh-pass-2')

        // when:
        runScript("withCredentials_Jenkinsfile")

        // then:
        assertJobStatusSuccess()
        assertCallStack().contains("echo(User/Pass = test-user-1/test-password-1)")
        assertCallStack().contains("echo(Nested User/Pass = test-user-2/test-password-2)")
        assertCallStack().contains("echo(Restored User/Pass = test-user-1/test-password-1)")
        assertCallStack().contains("echo(Cleared User/Pass = null/null)")

        assertCallStack().contains("echo(Docker = docker-pass-1)")
        assertCallStack().contains("echo(Nested Docker = docker-pass-2)")
        assertCallStack().contains("echo(Restored Docker = docker-pass-1)")
        assertCallStack().contains("echo(Cleared Docker = null)")

        assertCallStack().contains("echo(SSH = ssh-pass-1)")
        assertCallStack().contains("echo(Nested SSH = ssh-pass-2)")
        assertCallStack().contains("echo(Restored SSH = ssh-pass-1)")
        assertCallStack().contains("echo(Cleared SSH = null)")
    }
}
