# Jenkins Pipeline Unit testing framework

Jenkins Pipeline Unit is a testing framework for unit testing Jenkins pipelines, written in
[Groovy Pipeline DSL](https://jenkins.io/doc/book/pipeline/).

[![Build Status](https://travis-ci.org/lesfurets/JenkinsPipelineUnit.svg?branch=master)](https://travis-ci.org/lesfurets/JenkinsPipelineUnit)

If you use Jenkins as your CI workhorse (like us @ [lesfurets.com](https://www.lesfurets.com)) and you enjoy writing _pipeline-as-code_,
you already know that pipeline code is very powerful but can get pretty complex.

This testing framework lets you write unit tests on the configuration and conditional logic of the pipeline code, by providing a mock execution of the pipeline.
You can mock built-in Jenkins commands, job configurations, see the stacktrace of the whole execution and even track regressions.

## Usage

### Add to your project as test dependency

Maven: 

```xml
    <dependency>
      <groupId>com.lesfurets</groupId>
      <artifactId>jenkins-pipeline-unit</artifactId>
      <version>0.11</version>
      <scope>test</scope>
    </dependency>
```

Gradle:

```groovy
testCompile group:'com.lesfurets', name:'jenkins-pipeline-unit', version:'0.11'
```

### Start writing tests

You can write your tests in Groovy or Java 8, using the test framework you prefer.
The easiest entry point is extending the abstract class `BasePipelineTest`, which initializes the framework with JUnit.

Let's say you wrote this awesome pipeline script, which builds and tests your project :
 
 ```groovy
def execute() {
    node() {
        def utils = load 'src/main/jenkins/lib/utils.jenkins'
        String revision = stage('Checkout') {
            checkout scm
            return utils.currentRevision()
        }
        gitlabBuilds(builds: ['build', 'test']) {
            stage('build') {
                gitlabCommitStatus('build') {
                    sh "mvn clean package -DskipTests -DgitRevision=$revision"
                }
            }

            stage('test') {
                gitlabCommitStatus('test') {
                    sh "mvn verify -DgitRevision=$revision"
                }
            }
        }
    }
}
return this
```

Now using the Jenkins Pipeline Unit you can unit test if it does the job :

```groovy
import com.lesfurets.jenkins.unit.BasePipelineTest

class TestExampleJob extends BasePipelineTest {
        
        //...
        
        @Test
        void should_execute_without_errors() throws Exception {
            def script = loadScript('job/exampleJob.jenkins')
            script.execute()
            printCallStack()
        }
}

```

This test will print the call stack of the execution :

```text
exampleJob.run()
exampleJob.execute()
    exampleJob.node(groovy.lang.Closure)
        exampleJob.load(src/main/jenkins/lib/utils.jenkins)
        utils.run()
        exampleJob.stage(Checkout, groovy.lang.Closure)
        exampleJob.checkout({$class=GitSCM, branches=[{name=feature_test}], doGenerateSubmoduleConfigurations=false, extensions=[], submoduleCfg=[], userRemoteConfigs=[{credentialsId=gitlab_git_ssh, url=github.com/lesfurets/JenkinsPipelineUnit.git}]})
        utils.currentRevision()
            utils.sh({returnStdout=true, script=git rev-parse HEAD})
        exampleJob.gitlabBuilds({builds=[build, test]}, groovy.lang.Closure)
        exampleJob.stage(build, groovy.lang.Closure)
            exampleJob.gitlabCommitStatus(build, groovy.lang.Closure)
                exampleJob.sh(mvn clean package -DskipTests -DgitRevision=bcc19744)
        exampleJob.stage(test, groovy.lang.Closure)
            exampleJob.gitlabCommitStatus(test, groovy.lang.Closure)
                exampleJob.sh(mvn verify -DgitRevision=bcc19744)
```

### Mock Jenkins commands

You can register interceptors to mock Jenkins commands, which may or may not return a result.

```groovy
    @Override
    @Before
    void setUp() throws Exception {
        super.setUp()
        helper.registerAllowedMethod('sh', [Map.class], {c -> 'bcc19744'})
        helper.registerAllowedMethod('timeout', [Map.class, Closure.class], null)
        helper.registerAllowedMethod(method('readFile', String.class), { file ->
            return Files.contentOf(new File(file), Charset.forName('UTF-8'))
        })
    }
```

The test helper already includes some mocks, but the list is far from complete. 
You need to _register allowed methods_ if you want to override these mocks and add others. 
Note that you need to provide a method signature and a callback (closure or lambda) in order to allow a method.
Any method call which is not recognized will throw an exception.

You can take a look at the `BasePipelineTest` class to have the short list of allowed methods.

Some tricky methods such as `load` and `parallel` are implemented directly in the helper.
If you want to override those, make sure that you extend the `PipelineTestHelper` class.

### Analyze the mock execution

The helper registers every method call to provide a stacktrace of the mock execution.

```groovy

@Test
void should_execute_without_errors() throws Exception {
    loadScript('Jenkinsfile')
    assertThat(helper.callStack.findAll { call ->
        call.methodName == 'sh'
    }.any { call ->
        callArgsToString(call).contains('mvn verify')
    }).isTrue()
    assertJobStatusSuccess()
}

```

This will check as well `mvn verify` has been called during the job execution. 

### Compare the callstacks with a previous implementation

One other use of the callstacks is to check your pipeline executions for possible regressions.
You have a dedicated method you can call if you extend `BaseRegressionTest`:

```groovy
    @Test
    void testNonReg() throws Exception {
        def script = loadScript('job/exampleJob.jenkins')
        script.execute()
        super.testNonRegression('example', false)
    }
```

This will compare the current callstack of the job to the one you have in a text callstack reference file.
To overwrite this file with new callstack, just set the `updateReference` to true when calling testNonRegression:

```groovy
super.testNonRegression('example', true)
```

You then can go ahead and commit this change in your SCM to check in the change.

## Configuration

The abstract class `BasePipelineTest` configures the helper with useful conventions: 

- It looks for pipeline scripts in your project in root (`./.`) and `src/main/jenkins` paths.
- Jenkins pipelines let you load other scripts from a parent script with `load` command.
However `load` takes the full path relative to the project root.
The test helper mock successfully the `load` command to load the scripts.
To make relative paths work, you need to configure the path of the project where your pipeline scripts are,
which defaults to `.`.
- Pipeline script extension, which defaults to jenkins (matches any `*.jenkins` file)

Overriding these default values is easy:

```groovy

class TestExampleJob extends BasePipelineTest {

    @Override
    @Before
    void setUp() throws Exception {
        helper.baseScriptRoot = 'jenkinsJobs'
        helper.roots += 'src/main/groovy'
        helper.extension = 'pipeline'
        super.setUp()
    }
    
}

```

This will work fine for such a project structure:

```
 jenkinsJobs
 └── src
     ├── main
     │   └── groovy
     │       └── ExampleJob.pipeline
     └── test
         └── groovy
             └── TestExampleJob.groovy
```

## Note on CPS

If you already fiddled with Jenkins pipeline DSL, you experienced strange errors during execution on Jenkins.
This is because Jenkins does not directly execute your pipeline in Groovy,
but transforms the pipeline code into an intermediate format to in order to run Groovy code in
[Continuation Passing Style](https://en.wikipedia.org/wiki/Continuation-passing_style) (CPS).
 
The usual errors are partly due to the 
[the sandboxing Jenkins applies](https://wiki.jenkins-ci.org/display/JENKINS/Script+Security+Plugin#ScriptSecurityPlugin-GroovySandboxing) 
for security reasons, and partly due to the
[serializability Jenkins imposes](https://github.com/jenkinsci/pipeline-plugin/blob/master/TUTORIAL.md#serializing-local-variables).

Jenkins requires that at each execution step, the whole script context is serializable, in order to stop and resume the job execution.
To simulate this aspect, CPS versions of the helpers transform your scripts into the CPS format and check if at each step your script context is serializable. 

To use this _*experimental*_ feature, you can use the abstract class `BasePipelineTestCPS` instead of `BasePipelineTest`.
You may see some changes in the call stacks that the helper registers.
Note also that the serialization used to test is not the same as what Jenkins uses. 
You may find some incoherence in that level.
