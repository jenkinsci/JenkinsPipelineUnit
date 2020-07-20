package com.lesfurets.jenkins.unit.declarative

import org.junit.*

class TestDeclarativePipeline extends DeclarativePipelineTest {

    @Before
    @Override
    void setUp() throws Exception {
        scriptRoots = ['src/test/jenkins/jenkinsfiles']
        scriptExtension = ''
        super.setUp()
    }

    @Test void jenkinsfile_success() throws Exception {
        def script = runScript('Declarative_Jenkinsfile')
        printCallStack()
        assertCallStackContains('pipeline unit tests PASSED')
        assertCallStackContains('pipeline unit tests completed')
        assertCallStackContains('pipeline unit tests post CLEANUP')
        assertCallStack().doesNotContain('pipeline unit tests UNSUCCESSFUL')
        assertJobStatusSuccess()
    }

    @Test void jenkinsfile_failure() throws Exception {
        helper.registerAllowedMethod('sh', [String.class], { String cmd ->
            updateBuildStatus('FAILURE')
        })
        runScript('Declarative_Jenkinsfile')
        printCallStack()
        assertJobStatusFailure()
        assertCallStack()
        assertCallStack().contains('pipeline unit tests FAILED')
        assertCallStackContains('pipeline unit tests post CLEANUP')
        assertCallStack().contains('pipeline unit tests UNSUCCESSFUL')
        assertCallStackContains('pipeline unit tests completed')
    }

    @Test void jenkinsfile_aborted() throws Exception {
        helper.registerAllowedMethod('sh', [String.class], { String cmd ->
            updateBuildStatus('ABORTED')
        })
        runScript('Declarative_Jenkinsfile')
        printCallStack()
        assertJobStatusAborted()
        assertCallStack()
        assertCallStack().contains('pipeline unit tests ABORTED')
        assertCallStackContains('pipeline unit tests post CLEANUP')
        assertCallStack().contains('pipeline unit tests UNSUCCESSFUL')
        assertCallStackContains('pipeline unit tests completed')
    }

    @Test void jenkinsfile_fixed() throws Exception {
        helper.registerAllowedMethod('sh', [String.class], { String cmd ->
            addPreviousBuild('FAILURE')
            updateBuildStatus('SUCCESS')
        })
        runScript('Declarative_Jenkinsfile')
        printCallStack()
        assertJobStatusSuccess()
        assertCallStack()
        assertCallStackContains('pipeline unit tests PASSED')
        assertCallStackContains('pipeline unit tests completed')
        assertCallStackContains('pipeline unit tests post CLEANUP')
        assertCallStackContains('pipeline unit tests results have CHANGED')
        assertCallStackContains('pipeline unit tests have been FIXED')
    }

    @Test void jenkinsfile_regression() throws Exception {
        helper.registerAllowedMethod('sh', [String.class], { String cmd ->
            addPreviousBuild('SUCCESS')
            updateBuildStatus('UNSTABLE')
        })
        runScript('Declarative_Jenkinsfile')
        printCallStack()
        assertJobStatusUnstable()
        assertCallStack()

        assertCallStackContains('pipeline unit tests completed')
        assertCallStackContains('pipeline unit tests have gone UNSTABLE')
        assertCallStackContains('pipeline unit tests results have CHANGED')
        assertCallStackContains('pipeline unit tests UNSUCCESSFUL')
        assertCallStackContains('pipeline unit tests post REGRESSION')
        assertCallStackContains('pipeline unit tests post CLEANUP')

    }

    @Test void should_params() throws Exception {
        runScript('Params_Jenkinsfile')
        printCallStack()
        assertCallStack().contains('Hello Mr Jenkins')
        assertJobStatusSuccess()
    }

    @Test void when_branch() throws Exception {
        addEnvVar('BRANCH_NAME', 'production')
        runScript('Branch_Jenkinsfile')
        printCallStack()
        assertCallStack().contains('Deploying')
        assertJobStatusSuccess()
    }

    @Test void when_branch_not() throws Exception {
        addEnvVar('BRANCH_NAME', 'master')
        runScript('Branch_Jenkinsfile')
        printCallStack()
        assertCallStack().contains('Skipping stage Example Deploy')
        assertJobStatusSuccess()
    }

    @Test void when_tag() throws Exception {
        addEnvVar('TAG_NAME', 'v1.1.1')
        runScript('Tag_Jenkinsfile')
        printCallStack()
        assertCallStack().contains('Deploying')
        assertJobStatusSuccess()
    }

    @Test void when_tag_not() throws Exception {
        addEnvVar('TAG_NAME', 'someothertag')
        runScript('Tag_Jenkinsfile')
        printCallStack()
        assertCallStack().contains('Skipping stage Example Deploy')
        assertJobStatusSuccess()
    }

    @Test void should_agent() throws Exception {
        runScript('Agent_Jenkinsfile')
        printCallStack()
        assertCallStack().contains('openjdk:8-jre')
        assertCallStack().contains('maven:3-alpine')
        assertJobStatusSuccess()
    }

    @Test void should_credentials() throws Exception {
        addCredential('my-prefined-secret-text', 'something_secret')
        runScript('Credentials_Jenkinsfile')
        printCallStack()
        assertCallStack().contains('AN_ACCESS_KEY:something_secret')
        assertJobStatusSuccess()
    }

    @Test void should_parallel() throws Exception {
        runScript('Parallel_Jenkinsfile')
        printCallStack()
        assertCallStack().contains('sh(run-tests.exe)')
        assertCallStack().contains('sh(run-tests.sh)')
        assertJobStatusSuccess()
    }

    @Test void should_sub_stages() throws Exception {
        runScript('ComplexStages_Jenkinsfile')
        printCallStack()
        assertCallStack().contains('mvn build')
        assertCallStack().contains('mvn --version')
        assertCallStack().contains('java -version')
        assertJobStatusSuccess()
    }

    @Test void should_environment() throws Exception {
        runScript('Environment_Jenkinsfile')
        printCallStack()
        assertCallStack().contains('echo(LEVAR1 LE NEW VALUE)')
        assertCallStack().contains('echo(LEVAR2 A COPY OF LE NEW VALUE in build#1)')
        assertJobStatusSuccess()
    }

    @Test void not_running_stage_after_failure() throws Exception {
        runScript('StageFailed_Jenkinsfile')
        printCallStack()
        assertCallStack().contains('Stage "Not executed stage" skipped due to earlier failure(s)')
        assertJobStatusFailure()
    }

    @Test(expected = MissingPropertyException)
    void should_non_valid_fail() throws Exception {
        try {
            runScript('Non_Valid_Jenkinsfile')
        } catch (e) {
            e.printStackTrace()
            throw e
        } finally {
            printCallStack()
        }
    }

    @Test void should_not_leave_environment_dirty() throws Exception {
        runScript('WithEnv_Jenkinsfile')
        printCallStack()
        assertCallStack().contains('echo(SOMEVAR inside closure = SOMEVAL)')
        assertCallStack().contains('echo(SOMEVAR overlapping inside closure = SOMEVAL)')
        assertCallStack().contains('echo(SOMEVAR restored inside closure = SOMEVAL)')
        assertCallStack().contains('echo(SOMEVAR outside closure = null)')
    }

    @Test void agent_with_param_label() throws Exception {
        runScript('AgentParam_Jenkinsfile')
        printCallStack()
        assertCallStack().contains('aSlave')
        assertJobStatusSuccess()
    }

    @Test void agent_with_mock_param_label() throws Exception {
        def params = binding.getVariable('params')
        params['SLAVE'] = 'mockSlave'
        runScript('AgentParam_Jenkinsfile')
        printCallStack()
        assertCallStack().contains('mockSlave')
        assertJobStatusSuccess()
    }

    @Test void should_agent_with_environment() throws Exception {
        def env = binding.getVariable('env')
        env['ENV_VAR'] = 'ENV VAR'
        env['some_env_var'] = 'some env var'
        runScript('Agent_env_Jenkinsfile')
        printCallStack()
        assertJobStatusSuccess()
    }

    @Ignore @Test void should_agent_with_bindings() throws Exception {
        final def binding_var = 'a binding var'
        binding.setVariable('binding_var', binding_var)

        helper.registerAllowedMethod('getBinding_var', {
            return binding_var
        })

        runScript('Agent_bindings_Jenkinsfile')
        printCallStack()
        assertJobStatusSuccess()
    }
}
