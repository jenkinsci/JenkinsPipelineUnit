package com.lesfurets.jenkins

import com.lesfurets.jenkins.unit.BasePipelineTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class TestCatchError  extends BasePipelineTest {

    @Override
    @BeforeEach
    void setUp() throws Exception {
        super.setUp()
    }

    @Test
    void should_fail_with_fail_before_and_SuccesCatch() throws Exception {
        def script = runInlineScript("""
            node() {
                stage('test') {
                    error 'error'
                    catchError(buildResult: 'SUCCESS') {
                        throw new Exception()
                    }
                }
            }
    """)
        assertJobStatusFailure()
    }

    @Test
    void should_unstable_with_unstable_before_and_SuccesCatch() throws Exception {
        def script = runInlineScript("""
            node() {
                stage('test') {
                    unstable 'unstable'
                    catchError(buildResult: 'SUCCESS') {
                        throw new Exception()
                    }
                }
            }
    """)
        assertJobStatusUnstable()
    }

    @Test
    void should_succes_with_SuccesCatch() throws Exception {
        def script = runInlineScript("""
            node() {
                stage('test') {
                    catchError(buildResult: 'SUCCESS') {
                        throw new Exception()
                    }
                }
            }
    """)
        assertJobStatusSuccess()
    }

    @Test
    void should_unstable_with_UnstableCatch() throws Exception {
        def script = runInlineScript("""
            node() {
                stage('test') {
                    catchError(buildResult: 'UNSTABLE') {
                        throw new Exception()
                    }
                }
            }
    """)
        assertJobStatusUnstable()
    }

    @Test
    void should_fail_with_no_parameter() throws Exception {
        def script = runInlineScript("""
            node() {
                stage('test') {
                    catchError() {
                        throw new Exception()
                    }
                }
            }
    """)
        assertJobStatusFailure()
    }

}
