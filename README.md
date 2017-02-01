# Jenkins Pipeline Unit testing framework

Jenkins Pipeline Unit is a testing framework used to implement unit tests for the Jenkins pipelines. It is written in Groovy and Jenkins pipeline are written in [Groovy Pipeline DSL](https://jenkins.io/doc/book/pipeline/).

[![Build Status](https://travis-ci.org/lesfurets/pipeline-test-helper.svg?branch=master)](https://travis-ci.org/lesfurets/pipeline-test-helper)

If you use Jenkins as your CI workhorse (like us @ [lesfurets.com](https://www.lesfurets.com)) and you enjoy writing _pipeline-as-code_, you already know that pipeline code is very powerful but can get pretty complex.

This testing framework lets you write unit tests on the configuration and conditional logic of the pipeline code, by providing a mock execution of the pipeline. You can mock built-in Jenkins commands, job configurations, see the stacktrace of the whole execution and even track regressions.

## Usage

### Add to your project as test dependency

Maven: 

```xml
    <dependency>
      <groupId>com.lesfurets</groupId>
      <artifactId>jenkins-pipeline-unit</artifactId>
      <version>0.10</version>
      <scope>test</scope>
    </dependency>
```

Gradle:

```groovy
testCompile group:'com.lesfurets', name:'jenkins-pipeline-unit', version:'0.10'
```

### Start writing tests

You can write your tests in Java or Groovy, using the test framework you prefer. The easiest entry point is extending the abstract class `BasePipelineTest`.

```groovy
import com.lesfurets.jenkins.helpers.BasePipelineTest

class TestExampleJob extends BasePipelineTest {

        @Test
        void should_execute_without_errors() throws Exception {
            def script = loadScript("job/exampleJob.jenkins")
            script.execute()
            printCallStack()
        }
}

```

### Mock Jenkins commands

You can register interceptors to mock Jenkins commands, which may or may not return a result.

```groovy
    @Override
    @Before
    void setUp() throws Exception {
        super.setUp()
        helper.registerAllowedMethod("timeout", [Map.class, Closure.class], null)
        helper.registerAllowedMethod(method("readFile", String.class), { file ->
            return Files.contentOf(new File(file), Charset.forName("UTF-8"))
        })
    }
```

The test helper already includes some mocks, but the list is far from complete. You need to _register allowed methods_ if you want to override these mocks and add others. Note that you need to provide a method signature and a callback (closure or lambda) in order to allow a method. Any method call which is not recognized will throw an exception.

Some tricky methods such as `load` and `parallel` are implemented directly in the helper. If you want to override those, make sure that you extend the `PipelineTestHelper` class.

### Analyze the mock execution

The helper registers every method call to provide a stacktrace of the mock execution.

```groovy

@Test
void should_execute_without_errors() throws Exception {
    loadScript("Jenkinsfile")
    assertThat(helper.callStack.findAll { call ->
        call.methodName == "sh"
    }.any { call ->
        callArgsToString(call).contains("mvn verify")
    }).isTrue()
    assertJobStatusSuccess()
}

```

This will check as well `mvn verify` has been called during the job execution. 

### Compare the callstacks with a previous implementation

You have a dedicated method you can call if you override BaseRegressionTest:

```groovy
    @Test
    void testNonReg() throws Exception {
        def script = loadScript("job/exampleJob.jenkins")
        script.execute()
        super.testNonRegression("example", false)
    }
```

This will compare the current callstack of the job to the one you have in a text callstack reference file. To update this file, just set the `updateReference` to true when calling testNonRegression:

```groovy
super.testNonRegression("example", true)
```

## Configuration

The abstract class `BasePipelineTest` configures the helper with useful conventions: 

- It looks for pipeline scripts in your project in root (`./.`) and `src/main/jenkins` paths.
- Jenkins pipelines let you load other scripts from a parent script with `load` command. However `load` takes the full path relative to the project root. The test helper mock successfully the `load` command to load the scripts. To make relative paths work, you need to configure the path of the project where your pipeline scripts are, which defaults to `production/jenkins/`
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

If you already fiddled with Jenkins pipeline DSL, you experienced strange errors during execution on Jenkins. This is because Jenkins does not directly execute your pipeline in Groovy, but transforms the pipeline code into an intermediate format to in order to run Groovy code in [Continuation Passing Style](https://en.wikipedia.org/wiki/Continuation-passing_style) (CPS).
 
The usual errors are partly due to the [the sandboxing Jenkins applies](https://wiki.jenkins-ci.org/display/JENKINS/Script+Security+Plugin#ScriptSecurityPlugin-GroovySandboxing) for security reasons, and partly due to the [serializability Jenkins imposes](https://github.com/jenkinsci/pipeline-plugin/blob/master/TUTORIAL.md#serializing-local-variables).

Jenkins requires that at each execution step, the whole script context is serializable, in order to stop and resume the job execution. To simulate this aspect, CPS versions of the helpers transform your scripts into the CPS format and check if at each step your script context is serializable. 

To use this _*experimental*_ feature, you can use the abstract class `BasePipelineTestCPS` instead of `BasePipelineTest`. You may see some changes in the call stacks that the helper registers. Note also that the serialization used to test is not the same as what Jenkins uses. You may find some incoherence in that level.

