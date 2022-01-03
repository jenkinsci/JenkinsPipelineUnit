package com.lesfurets.jenkins.unit.declarative

import org.junit.Before
import org.junit.Test

class TestDeclarativePipeline extends DeclarativePipelineTest {

    @Before
    @Override
    void setUp() throws Exception {
        scriptRoots += 'src/test/jenkins/jenkinsfiles'
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
        assertCallStackContains('Skipping stage Checkout')
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

    @Test void when_not_branch_master() throws Exception {
        addEnvVar('BRANCH_NAME', 'dev')
        runScript('not_Jenkinsfile')
        printCallStack()
        assertCallStack().contains('Running')
        assertJobStatusSuccess()
    }

    @Test void when_anyOf_branch_not() throws Exception {
        addEnvVar('BRANCH_NAME', 'master')
        runScript('AnyOf_Jenkinsfile')
        printCallStack()
        assertCallStack().contains('Skipping stage Example anyOf branch')
        assertJobStatusSuccess()
    }

    @Test void when_anyOf_branch_release() throws Exception {
        addEnvVar('BRANCH_NAME', 'release/A.B.C')
        runScript('AnyOf_Jenkinsfile')
        printCallStack()
        assertCallStack().contains('Executing anyOf with branch')
        assertJobStatusSuccess()
    }

    @Test void when_anyOf_branch_production() throws Exception {
        addEnvVar('BRANCH_NAME', 'production')
        runScript('AnyOf_Jenkinsfile')
        printCallStack()
        assertCallStack().contains('Executing anyOf with branch')
        assertJobStatusSuccess()
    }

    @Test void when_anyOf_branch_pattern_main() throws Exception {
        addEnvVar('BRANCH_NAME', 'main-2')
        runScript('AnyOf_Jenkinsfile')
        printCallStack()
        assertCallStack().contains('Executing anyOf with branch')
        assertJobStatusSuccess()
    }

    @Test void when_anyOf_branch_feature() throws Exception {
        addEnvVar('BRANCH_NAME', 'feature/jenkins')
        runScript('AnyOf_Jenkinsfile')
        printCallStack()
        assertCallStack().contains('Executing anyOf with branch')
        assertJobStatusSuccess()
    }

    @Test void when_anyOf_branch_pattern_not() throws Exception {
        addEnvVar('BRANCH_NAME', '?')
        runScript('AnyOf_Jenkinsfile')
        printCallStack()
        assertCallStack().contains('Skipping stage Example anyOf branch')
        assertJobStatusSuccess()
    }

    @Test void when_anyOf_tag_latest() throws Exception {
        addEnvVar('TAG_NAME', 'latest')
        runScript('AnyOf_Jenkinsfile')
        printCallStack()
        assertCallStack().contains('Executing anyOf with tag')
        assertJobStatusSuccess()
    }

    @Test void when_anyOf_tag_release_pattern() throws Exception {
        addEnvVar('TAG_NAME', 'release-X.Y.Z')
        runScript('AnyOf_Jenkinsfile')
        printCallStack()
        assertCallStack().contains('Executing anyOf with tag')
        assertJobStatusSuccess()
    }

    @Test void when_anyOf_tag_v_pattern() throws Exception {
        addEnvVar('TAG_NAME', 'v-X.Y.Z')
        runScript('AnyOf_Jenkinsfile')
        printCallStack()
        assertCallStack().contains('Executing anyOf with tag')
        assertJobStatusSuccess()
    }

    @Test void when_anyOf_tag_version_pattern() throws Exception {
        addEnvVar('TAG_NAME', 'version-X.Y.Z')
        runScript('AnyOf_Jenkinsfile')
        printCallStack()
        assertCallStack().contains('Executing anyOf with tag')
        assertJobStatusSuccess()
    }

    @Test void when_anyOf_tag_not() throws Exception {
        addEnvVar('TAG_NAME', '?')
        runScript('AnyOf_Jenkinsfile')
        printCallStack()
        assertCallStack().contains('Skipping stage Example anyOf tag')
        assertJobStatusSuccess()
    }

    @Test void when_anyOf_changeRequest() throws Exception {
        addEnvVar('CHANGE_TARGET', 'release/1.2.3')
        runScript('AnyOf_Jenkinsfile')
        printCallStack()
        assertCallStack().contains('Executing anyOf with changeRequest')
        assertJobStatusSuccess()
    }

    @Test void when_anyOf_changeRequest_not() throws Exception {
        addEnvVar('CHANGE_TARGET', 'master')
        runScript('AnyOf_Jenkinsfile')
        printCallStack()
        assertCallStack().contains('Skipping stage Example anyOf changeRequests')
        assertJobStatusSuccess()
    }

    @Test void when_anyOf_expression_not() throws Exception {
        addEnvVar('SHOULD_EXECUTE', 'false')
        runScript('AnyOf_Jenkinsfile')
        printCallStack()
        assertCallStack().contains('Skipping stage Example anyOf expression')
        assertJobStatusSuccess()
    }

    @Test void when_anyOf_expression() throws Exception {
        addEnvVar('SHOULD_EXECUTE', 'true')
        runScript('AnyOf_Jenkinsfile')
        printCallStack()
        assertCallStack().contains('Executing anyOf with expression')
        assertJobStatusSuccess()
    }

    @Test void when_allOf_expression() throws Exception {
        addEnvVar('SHOULD_EXECUTE', 'true')
        addEnvVar('SHOULD_ALSO_EXECUTE', 'true')
        runScript('AllOf_Jenkinsfile')
        printCallStack()
        assertCallStack().contains('Executing allOf with expression')
        assertJobStatusSuccess()
    }

    @Test void when_allOf_expression_first_false() throws Exception {
        addEnvVar('SHOULD_EXECUTE', 'false')
        addEnvVar('SHOULD_ALSO_EXECUTE', 'true')
        runScript('AllOf_Jenkinsfile')
        printCallStack()
        assertCallStack().contains('Skipping stage Example allOf expression')
        assertJobStatusSuccess()
    }

    @Test void when_allOf_expression_second_false() throws Exception {
        addEnvVar('SHOULD_EXECUTE', 'true')
        addEnvVar('SHOULD_ALSO_EXECUTE', 'false')
        runScript('AllOf_Jenkinsfile')
        printCallStack()
        assertCallStack().contains('Skipping stage Example allOf expression')
        assertJobStatusSuccess()
    }

    @Test void when_allOf_expression_both_false() throws Exception {
        addEnvVar('SHOULD_EXECUTE', 'false')
        addEnvVar('SHOULD_ALSO_EXECUTE', 'false')
        runScript('AllOf_Jenkinsfile')
        printCallStack()
        assertCallStack().contains('Skipping stage Example allOf expression')
        assertJobStatusSuccess()
    }

    @Test void when_allOf_branches() throws Exception {
        addEnvVar('BRANCH_NAME', 'production')
        runScript('AllOf_Jenkinsfile')
        printCallStack()
        assertCallStack().contains('Skipping stage Example allOf branches')
        assertJobStatusSuccess()
    }

    @Test void when_allOf_tags() throws Exception {
        addEnvVar('TAG_NAME', 'latest')
        runScript('AllOf_Jenkinsfile')
        printCallStack()
        assertCallStack().contains('Skipping stage Example allOf tags')
        assertJobStatusSuccess()
    }

    @Test void when_allOf_changerequests() throws Exception {
        addEnvVar('CHANGE_TARGET', 'develop')
        addEnvVar('CHANGE_BRANCH', 'feature/ABC-123 new feature')
        runScript('AllOf_Jenkinsfile')
        printCallStack()
        assertCallStack().contains('Executing allOf with changeRequest')
        assertJobStatusSuccess()
    }

    @Test void when_allOf_changerequests_not() throws Exception {
        addEnvVar('CHANGE_TARGET', 'develop')
        addEnvVar('CHANGE_BRANCH', 'hotfix/ABC-123 problem solved')
        runScript('AllOf_Jenkinsfile')
        printCallStack()
        assertCallStack().contains('Skipping stage Example allOf changeRequests')
        assertJobStatusSuccess()
    }

    @Test void when_nested_when_allOf_anyOf_expression_1() throws Exception {
        addEnvVar('OPTIONAL_1', 'false')
        addEnvVar('OPTIONAL_2', 'false')
        addEnvVar('OPTIONAL_3', 'false')
        addEnvVar('OPTIONAL_4', 'false')
        runScript('Nested_AllOf_And_AnyOf_Jenkinsfile')
        printCallStack()
        assertCallStack().contains('Skipping stage Example nested when allOf anyOf expression')
        assertJobStatusSuccess()
    }

    @Test void when_nested_when_allOf_anyOf_expression_2() throws Exception {
        addEnvVar('OPTIONAL_1', 'true')
        addEnvVar('OPTIONAL_2', 'false')
        addEnvVar('OPTIONAL_3', 'false')
        addEnvVar('OPTIONAL_4', 'false')
        runScript('Nested_AllOf_And_AnyOf_Jenkinsfile')
        printCallStack()
        assertCallStack().contains('Skipping stage Example nested when allOf anyOf expression')
        assertJobStatusSuccess()
    }

    @Test void when_nested_when_allOf_anyOf_expression_3() throws Exception {
        addEnvVar('OPTIONAL_1', 'false')
        addEnvVar('OPTIONAL_2', 'true')
        addEnvVar('OPTIONAL_3', 'false')
        addEnvVar('OPTIONAL_4', 'false')
        runScript('Nested_AllOf_And_AnyOf_Jenkinsfile')
        printCallStack()
        assertCallStack().contains('Skipping stage Example nested when allOf anyOf expression')
        assertJobStatusSuccess()
    }

    @Test void when_nested_when_allOf_anyOf_expression_4() throws Exception {
        addEnvVar('OPTIONAL_1', 'false')
        addEnvVar('OPTIONAL_2', 'false')
        addEnvVar('OPTIONAL_3', 'true')
        addEnvVar('OPTIONAL_4', 'false')
        runScript('Nested_AllOf_And_AnyOf_Jenkinsfile')
        printCallStack()
        assertCallStack().contains('Skipping stage Example nested when allOf anyOf expression')
        assertJobStatusSuccess()
    }

    @Test void when_nested_when_allOf_anyOf_expression_5() throws Exception {
        addEnvVar('OPTIONAL_1', 'false')
        addEnvVar('OPTIONAL_2', 'false')
        addEnvVar('OPTIONAL_3', 'false')
        addEnvVar('OPTIONAL_4', 'true')
        runScript('Nested_AllOf_And_AnyOf_Jenkinsfile')
        printCallStack()
        assertCallStack().contains('Skipping stage Example nested when allOf anyOf expression')
        assertJobStatusSuccess()
    }

    @Test void when_nested_when_allOf_anyOf_expression_6() throws Exception {
        addEnvVar('OPTIONAL_1', 'true')
        addEnvVar('OPTIONAL_2', 'false')
        addEnvVar('OPTIONAL_3', 'true')
        addEnvVar('OPTIONAL_4', 'false')
        runScript('Nested_AllOf_And_AnyOf_Jenkinsfile')
        printCallStack()
        assertCallStack().contains('Executing nested when allOf anyOf expression')
        assertJobStatusSuccess()
    }

    @Test void when_nested_when_allOf_anyOf_expression_7() throws Exception {
        addEnvVar('OPTIONAL_1', 'false')
        addEnvVar('OPTIONAL_2', 'true')
        addEnvVar('OPTIONAL_3', 'false')
        addEnvVar('OPTIONAL_4', 'true')
        runScript('Nested_AllOf_And_AnyOf_Jenkinsfile')
        printCallStack()
        assertCallStack().contains('Executing nested when allOf anyOf expression')
        assertJobStatusSuccess()
    }

    @Test void when_nested_when_anyOf_allOf_expression() throws Exception {
        addEnvVar('OPTIONAL_1', 'true')
        addEnvVar('OPTIONAL_2', 'true')
        addEnvVar('OPTIONAL_3', 'true')
        runScript('Nested_AnyOf_And_AllOf_Jenkinsfile')
        printCallStack()
        assertCallStack().contains('Executing nested when anyOf allOf expression')
        assertJobStatusSuccess()
    }

    @Test void when_nested_when_anyOf_allOf_expression_2() throws Exception {
        addEnvVar('OPTIONAL_1', 'false')
        addEnvVar('OPTIONAL_2', 'true')
        addEnvVar('OPTIONAL_3', 'true')
        runScript('Nested_AnyOf_And_AllOf_Jenkinsfile')
        printCallStack()
        assertCallStack().contains('Executing nested when anyOf allOf expression')
        assertJobStatusSuccess()
    }

    @Test void when_nested_when_anyOf_allOf_expression_3() throws Exception {
        addEnvVar('OPTIONAL_1', 'true')
        addEnvVar('OPTIONAL_2', 'false')
        addEnvVar('OPTIONAL_3', 'true')
        runScript('Nested_AnyOf_And_AllOf_Jenkinsfile')
        printCallStack()
        assertCallStack().contains('Executing nested when anyOf allOf expression')
        assertJobStatusSuccess()
    }

    @Test void when_nested_when_anyOf_allOf_expression_4() throws Exception {
        addEnvVar('OPTIONAL_1', 'true')
        addEnvVar('OPTIONAL_2', 'true')
        addEnvVar('OPTIONAL_3', 'false')
        runScript('Nested_AnyOf_And_AllOf_Jenkinsfile')
        printCallStack()
        assertCallStack().contains('Executing nested when anyOf allOf expression')
        assertJobStatusSuccess()
    }

    @Test void when_nested_when_anyOf_allOf_expression_5() throws Exception {
        addEnvVar('OPTIONAL_1', 'false')
        addEnvVar('OPTIONAL_2', 'false')
        addEnvVar('OPTIONAL_3', 'false')
        runScript('Nested_AnyOf_And_AllOf_Jenkinsfile')
        printCallStack()
        assertCallStack().contains('Skipping stage Example nested when anyOf allOf expression')
        assertJobStatusSuccess()
    }

    @Test void when_branch_using_explicit_equals_comparator() throws Exception {
        addEnvVar('BRANCH_NAME', 'develop')
        runScript('Branch_Jenkinsfile')
        printCallStack()
        assertCallStack().contains('EQUALS (explicit)')
        assertJobStatusSuccess()
    }

    @Test void when_branch_using_implicit_glob_comparator() throws Exception {
        addEnvVar('BRANCH_NAME', 'feature/xyz')
        runScript('Branch_Jenkinsfile')
        printCallStack()
        assertCallStack().contains('GLOB (implicit)')
        assertJobStatusSuccess()
    }

    @Test void when_branch_using_explicit_glob_comparator() throws Exception {
        addEnvVar('BRANCH_NAME', 'bugfix/xyz')
        runScript('Branch_Jenkinsfile')
        printCallStack()
        assertCallStack().contains('GLOB (explicit)')
        assertJobStatusSuccess()
    }

    @Test void when_branch_using_explicit_regexp_comparator() throws Exception {
        addEnvVar('BRANCH_NAME', 'hotfix/xyz')
        runScript('Branch_Jenkinsfile')
        printCallStack()
        assertCallStack().contains('REGEXP (explicit)')
        assertJobStatusSuccess()
    }

    @Test void when_branch_not() throws Exception {
        addEnvVar('BRANCH_NAME', 'master')
        runScript('Branch_Jenkinsfile')
        printCallStack()
        assertCallStack().contains('Skipping stage Example - EQUALS comparator')
        assertCallStack().contains('Skipping stage Example - GLOB comparator (implicit)')
        assertCallStack().contains('Skipping stage Example - GLOB comparator (explicit)')
        assertCallStack().contains('Skipping stage Example - REGEXP comparator')
        assertJobStatusSuccess()
    }

    @Test void when_change_request() throws Exception {
        addEnvVar('CHANGE_ID', '1')
        runScript('ChangeRequest_Jenkinsfile')
        printCallStack()
        assertCallStack().contains('Test only change requests')
        assertJobStatusSuccess()
    }

    @Test void when_change_request_not() throws Exception {
        runScript('ChangeRequest_Jenkinsfile')
        printCallStack()
        assertCallStack().contains('Skipping stage Example test')
        assertJobStatusSuccess()
    }

    @Test void when_change_request_target() throws Exception {
        addEnvVar('CHANGE_TARGET', 'develop')
        runScript('ChangeRequest_Jenkinsfile')
        printCallStack()
        assertCallStack().contains('Test change requests with develop branch as target')
        assertJobStatusSuccess()
    }

    @Test void when_change_request_branch_glob() throws Exception {
        addEnvVar('CHANGE_BRANCH', 'feature/ABC-123 feature branch')
        runScript('ChangeRequest_Jenkinsfile')
        printCallStack()
        assertCallStack().contains('Test change requests with any feature branch as source')
        assertJobStatusSuccess()
    }

    @Test void when_change_request_branch_glob_not() throws Exception {
        addEnvVar('CHANGE_BRANCH', 'hotfix/ABC-123 hotfix branch')
        runScript('ChangeRequest_Jenkinsfile')
        printCallStack()
        assertCallStack().contains('Skipping stage Example test branch feature')
        assertJobStatusSuccess()
    }

    @Test void when_change_request_author() throws Exception {
        addEnvVar('CHANGE_AUTHOR', 'unreliableUser')
        addEnvVar('CHANGE_AUTHOR_EMAIL', 'unreliableUser@com.com')
        addEnvVar('CHANGE_AUTHOR_DISPLAY_NAME', 'Unreliable User')
        runScript('ChangeRequest_Jenkinsfile')
        printCallStack()
        assertCallStack().contains('Test change requests with unreliableUser as author')
        assertJobStatusSuccess()
    }

    @Test void when_change_request_id_equals() throws Exception {
        addEnvVar('CHANGE_ID', '42')
        runScript('ChangeRequest_Jenkinsfile')
        printCallStack()
        assertCallStack().contains('Test change request with id 42')
        assertJobStatusSuccess()
    }

    @Test void when_change_request_id_equals_not() throws Exception {
        addEnvVar('CHANGE_ID', '042')
        runScript('ChangeRequest_Jenkinsfile')
        printCallStack()
        assertCallStack().contains('Skipping stage Example test id equals')
        assertJobStatusSuccess()
    }

    @Test void when_change_request_title_regexp() throws Exception {
        addEnvVar('CHANGE_TITLE', 'ABC-123 new feature')
        runScript('ChangeRequest_Jenkinsfile')
        printCallStack()
        assertCallStack().contains('Test change requests with alphanumeric titles')
        assertJobStatusSuccess()
    }

    @Test void when_change_request_title_regexp_not() throws Exception {
        addEnvVar('CHANGE_TITLE', '[ABC-123] new feature')
        runScript('ChangeRequest_Jenkinsfile')
        printCallStack()
        assertCallStack().contains('Skipping stage Example test title')
        assertJobStatusSuccess()
    }

    @Test void when_change_request_url_fork_glob() throws Exception {
        addEnvVar('CHANGE_URL', 'https://github.com/JenkinsPipelineUnit')
        addEnvVar('CHANGE_FORK', 'https://github.com/user/JenkinsPipelineUnit')
        runScript('ChangeRequest_Jenkinsfile')
        printCallStack()
        assertCallStack().contains('Test change requests with github in url and fork')
        assertJobStatusSuccess()
    }

    @Test void when_buildingTag() throws Exception {
        addEnvVar('TAG_NAME', 'release-1.0.0')
        runScript('Tag_Jenkinsfile')
        printCallStack()
        assertCallStack().contains('Generating Release Notes for any tag')
        assertJobStatusSuccess()
    }

    @Test void when_buildingTag_not() throws Exception {
        // no TAG_NAME variable defined
        runScript('Tag_Jenkinsfile')
        printCallStack()
        assertCallStack().contains('Skipping stage Example - EQUALS comparator')
        assertCallStack().contains('Skipping stage Example - GLOB comparator (implicit)')
        assertCallStack().contains('Skipping stage Example - GLOB comparator (explicit)')
        assertCallStack().contains('Skipping stage Example - REGEXP comparator')
        assertCallStack().contains('Skipping stage Example Release Notes')
        assertJobStatusSuccess()
    }

    @Test void when_tag_using_explicit_equals_comparator() throws Exception {
        addEnvVar('TAG_NAME', 'x.y.z')
        runScript('Tag_Jenkinsfile')
        printCallStack()
        assertCallStack().contains('EQUALS (explicit)')
        assertJobStatusSuccess()
    }

    @Test void when_tag_using_implicit_glob_comparator() throws Exception {
        addEnvVar('TAG_NAME', 'v1.0.0')
        runScript('Tag_Jenkinsfile')
        printCallStack()
        assertCallStack().contains('GLOB (implicit)')
        assertJobStatusSuccess()
    }

    @Test void when_tag_using_explicit_glob_comparator() throws Exception {
        addEnvVar('TAG_NAME', 'v-1.0.0')
        runScript('Tag_Jenkinsfile')
        printCallStack()
        assertCallStack().contains('GLOB (explicit)')
        assertJobStatusSuccess()
    }

    @Test void when_tag_using_explicit_regexp_comparator() throws Exception {
        addEnvVar('TAG_NAME', '1.0.0')
        runScript('Tag_Jenkinsfile')
        printCallStack()
        assertCallStack().contains('REGEXP (explicit)')
        assertJobStatusSuccess()
    }

    @Test void when_tag_not() throws Exception {
        addEnvVar('TAG_NAME', 'someothertag')
        runScript('Tag_Jenkinsfile')
        printCallStack()
        assertCallStack().contains('Skipping stage Example - EQUALS comparator')
        assertCallStack().contains('Skipping stage Example - GLOB comparator (implicit)')
        assertCallStack().contains('Skipping stage Example - GLOB comparator (explicit)')
        assertCallStack().contains('Skipping stage Example - REGEXP comparator')
        assertJobStatusSuccess()
    }

    @Test void should_agent() throws Exception {
        runScript('Agent_Jenkinsfile')
        printCallStack()
        assertCallStack().contains('openjdk:8-jre')
        assertCallStack().contains('maven:3-alpine')
        assertJobStatusSuccess()
    }

    @Test void should_kubernetes_agent() throws Exception {
        runScript('Kubernetes_Agent_Jenkinsfile')
        printCallStack()
        assertCallStack().contains('namespace: "jenkins"')
        assertCallStack().contains('image: jenkins/slave')
        assertJobStatusSuccess()
    }

    @Test void should_kubernetes_map_agent() throws Exception {
        runScript('Kubernetes_Map_Agent_Jenkinsfile')
        printCallStack()
        assertCallStack().contains('namespace: "jenkins"')
        assertCallStack().contains('image: jenkins/slave')
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

    @Test void should_parallel_nested_stages() throws Exception {
        runScript('Parallel_NestedStages_Jenkinsfile')
        printCallStack()
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
        params['AGENT'] = 'mockSlave'
        runScript('AgentParam_Jenkinsfile')
        printCallStack()
        assertCallStack().contains('mockSlave')
        assertJobStatusSuccess()
    }

    @Test void should_agent_with_environment() throws Exception {
        def env = binding.getVariable('env')
        env['ENV_VAR'] = 'ENV VAR_VALUE'
        env['some_env_var'] = 'some env var_value'
        runScript('Agent_env_Jenkinsfile')
        printCallStack()
        assertJobStatusSuccess()
    }

    @Test void should_agent_with_bindings() throws Exception {
        final def some_var = 'someVar'
        binding.setVariable('var', some_var)
        binding.setVariable('binding_var', some_var)

        runScript('Agent_bindings_Jenkinsfile')
        printCallStack()
        assertCallStack().contains('[label:someVar]')
        assertCallStack().contains('docker:[', 'image:someVar',)
        assertCallStack().contains('[label:someLabel]')
        assertCallStack().contains('echo(Deploy to someLabel)')
        assertJobStatusSuccess()
    }

    @Test void should_agent_with_empty_label() throws Exception {
        runScript('AgentEmptyLabel_Jenkinsfile')
        printCallStack()
        assertCallStack().contains('[label:]')
        assertCallStack().contains('echo(Hello using custom workspace and empty label)')
        assertJobStatusSuccess()
    }

    @Test void should_scope_this_in_closure() throws Exception {
        runScript('ThisScope_Jenkinsfile')
        printCallStack()
        assertCallStack().contains('writeFile({file=messages/messages.msg, text=text})')
    }
}
