package com.lesfurets.jenkins

import static org.assertj.core.api.Assertions.assertThat;

import com.lesfurets.jenkins.unit.BaseDeclarativePipelineTest
import org.junit.Before
import org.junit.Test

class TestBaseDeclarativePipelineTest extends BaseDeclarativePipelineTest {
    @Override
    String jenkinsfileName() {
        return "src/test/jenkins/job/declarative.jenkins"
    }

    @Override
    @Before
    void setUp() {
        super.setUp()
    }

    @Test
    void runAStage() {
        super.jenkinsfile.run()
        harness
            .runStage('init')
            .runClosureStep('steps')

        def call = helper.callStack.find { it.methodName == 'sh' }
        assertThat(call.args.toList()).isEqualTo(['echo \'hello world\''])
    }

    @Test
    void runASubStage1() {
        super.jenkinsfile.run()
        harness
                .runParallelSubStage('parallel build', 'build 1')
                .runClosureStep('steps')

        def call = helper.callStack.find { it.methodName == 'sh' }
        assertThat(call.args.toList()).isEqualTo(['echo \'hello build 1\''])
    }

    @Test
    void runASubStage2() {
        super.jenkinsfile.run()
        harness
                .runParallelSubStage('parallel build', 'build 2')
                .runClosureStep('steps')

        def call = helper.callStack.find { it.methodName == 'sh' }
        assertThat(call.args.toList()).isEqualTo(['echo \'hello build 2\''])
    }
}
