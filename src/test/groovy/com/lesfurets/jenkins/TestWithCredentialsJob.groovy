package com.lesfurets.jenkins

import com.lesfurets.jenkins.unit.BaseRegressionTest
import org.junit.Before
import org.junit.Test

import static org.junit.Assert.assertTrue

class TestWithCredentialsJob extends BaseRegressionTest {

    @Override
    @Before
    void setUp() throws Exception {
        scriptRoots += 'src/test/jenkins'
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
        runScript("job/withCredentials.jenkins")

        // then:
        assertJobStatusSuccess()
        testNonRegression("withCredentials")
    }
}
