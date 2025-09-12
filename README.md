# `JenkinsPipelineUnit` Testing Framework

Jenkins Pipeline Unit is a testing framework for unit testing Jenkins pipelines, written
in [Groovy Pipeline DSL](https://jenkins.io/doc/book/pipeline/).

[![Linux/Windows Build status](https://ci.jenkins.io/job/jenkinsci-libraries/job/JenkinsPipelineUnit/job/master/badge/icon)](https://ci.jenkins.io/blue/organizations/jenkins/jenkinsci-libraries%2FJenkinsPipelineUnit/activity?branch=master)
[![Mac Build status](https://github.com/jenkinsci/JenkinsPipelineUnit/actions/workflows/test.yml/badge.svg?branch=master)](https://github.com/jenkinsci/JenkinsPipelineUnit/actions?query=event%3Apush+branch%3Amaster)
[![GitHub release (latest SemVer)](https://img.shields.io/github/v/release/jenkinsci/JenkinsPipelineUnit?label=changelog)](https://github.com/jenkinsci/JenkinsPipelineUnit/releases)
[![Gitter chat](https://badges.gitter.im/gitterHQ/gitter.png)](https://gitter.im/JenkinsPipelineUnit)

If you use Jenkins as your CI workhorse (like us @
[lesfurets.com](https://www.lesfurets.com)) and you enjoy writing _pipeline-as-code_, you
already know that pipeline code is very powerful but can get pretty complex.

This testing framework lets you write unit tests on the configuration and conditional
logic of the pipeline code, by providing a mock execution of the pipeline. You can mock
built-in Jenkins commands, job configurations, see the stacktrace of the whole execution
and even track regressions.

# Table of Contents

1. [Usage](#usage)
1. [Configuration](#configuration)
1. [Declarative Pipeline](#declarative-pipelines)
1. [Testing Shared Libraries](#testing-pipelines-that-use-shared-libraries)
1. [Writing Testable Libraries](#writing-testable-libraries)
1. [Note On CPS](#note-on-cps)
1. [Contributing](#contributing)
1. [Demos and Examples](#demos-and-examples)

## Usage

### Add to Your Project as Test Dependency

JenkinsPipelineUnit requires Java 17, since this is also the minimum version required by
some library dependencies. Also note that JenkinsPipelineUnit is not currently compatible
with Groovy 4, please see [this
issue](https://github.com/jenkinsci/JenkinsPipelineUnit/issues/521) for more details.

**Note:** Starting from version `1.2`, artifacts are published to
`https://repo.jenkins-ci.org/releases`.

#### Maven

```xml
<repositories>
    <repository>
    <id>jenkins-ci-releases</id>
    <url>https://repo.jenkins-ci.org/releases/</url>
    </repository>
    ...
</repositories>

<dependencies>
    <dependency>
        <groupId>com.lesfurets</groupId>
        <artifactId>jenkins-pipeline-unit</artifactId>
        <version>1.9</version>
        <scope>test</scope>
    </dependency>
    ...
</dependencies>
```

#### Gradle

```groovy
repositories {
    maven { url 'https://repo.jenkins-ci.org/releases/' }
    ...
}

dependencies {
    testImplementation "com.lesfurets:jenkins-pipeline-unit:1.9"
    ...
}
```

### Start Writing Tests

You can write your tests in Groovy or Java, using the test framework you prefer. The
easiest entry point is extending the abstract class `BasePipelineTest`, which initializes
the framework with JUnit.

Let's say you wrote this awesome pipeline script, which builds and tests your project:

```groovy
def execute() {
    node() {
        String utils = load 'src/test/jenkins/lib/utils.jenkins'
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

Now using the Jenkins Pipeline Unit you can write a unit test to see if it does the job:

```groovy
import com.lesfurets.jenkins.unit.BasePipelineTest

class TestExampleJob extends BasePipelineTest {
    @Test
    void shouldExecuteWithoutErrors() {
        loadScript('job/exampleJob.jenkins').execute()
        printCallStack()
    }
}
```

This test will print the call stack of the execution, which should look like so:

```text
   exampleJob.run()
   exampleJob.execute()
      exampleJob.node(groovy.lang.Closure)
         exampleJob.load(src/test/jenkins/lib/utils.jenkins)
            utils.run()
         exampleJob.stage(Checkout, groovy.lang.Closure)
            exampleJob.checkout({$class=GitSCM, branches=[{name=feature_test}], extensions=[], userRemoteConfigs=[{credentialsId=gitlab_git_ssh, url=github.com/lesfurets/JenkinsPipelineUnit.git}]})
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

### Mocking Jenkins Variables

You can define both environment variables and job execution parameters.

```groovy
import com.lesfurets.jenkins.unit.BasePipelineTest

class TestExampleJob extends BasePipelineTest {
    @Override
    @BeforeEach
    void setUp() {
        super.setUp()
        // Assigns false to a job parameter ENABLE_TEST_STAGE
        addParam('ENABLE_TEST_STAGE', 'false')
        // Assigns 1.0.0-rc.1 to the environment variable TAG_NAME
        addEnvVar('TAG_NAME', '1.0.0-rc.1')
        // Defines the previous execution status
        binding.getVariable('currentBuild').previousBuild = [result: 'UNSTABLE']
    }

    @Test
    void verifyParam() {
        assertEquals('false', binding.getVariable('params')['ENABLE_TEST_STAGE'])
    }
}
```

After calling `super.setUp()`, the test `helper` instance is available, as well as many
helper methods. The test helper already provides basic variables such as a very simple
`currentBuild` definition. You can redefine them as you wish.

Note that `super.setUp()` must be called prior to using most features. This is commonly done
using your own `setUp` method, decorated with `@Override` and `@BeforeEach`.

Parameters added via `addParam` are immutable, which reflects the same behavior
in Jenkins. Attempting to modify the `params` map in the binding will result in an error.

### Mocking Jenkins Commands

You can register interceptors to mock pipeline methods, including Jenkins commands, which
may or may not return a result.

```groovy
import com.lesfurets.jenkins.unit.BasePipelineTest

class TestExampleJob extends BasePipelineTest {
    @Override
    @BeforeEach
    void setUp() {
        super.setUp()
        helper.registerAllowedMethod('sh', [Map]) { args -> return 'bcc19744' }
        helper.registerAllowedMethod('timeout', [Map, Closure], null)
        helper.registerAllowedMethod('timestamps', []) { println 'Printing timestamp' }
        helper.registerAllowedMethod('myMethod', [String, int]) { String s, int i ->
            println "Executing myMethod mock with args: '${s}', '${i}'"
        }
    }
}
```

The test helper already includes mocks for all base pipeline steps as well as a steps from
a few widely-used plugins. You need to _register allowed methods_ if you want to override
these mocks and add others. Note that you need to provide a method signature and a
callback (closure or lambda) in order to allow a method. Any method call which is not
recognized will throw an exception.

Please refer to the `BasePipelineTest` class for the list of currently supported mocks.

Some tricky methods such as `load` and `parallel` are implemented directly in the helper.
If you want to override those, make sure that you extend the `PipelineTestHelper` class.

### Mocking `readFile` and `fileExists`

The `readFile` and `fileExists` steps can be mocked to return a specific result for a
given file name. This can be useful for testing pipelines for which file operations can
influence subsequent steps. An example of such a testing scenario follows:

```groovy
// Jenkinsfile
node {
    stage('Process output') {
        if (fileExists('output') && readFile('output') == 'FAILED!!!') {
            currentBuild.result = 'FAILURE'
            error 'Build failed'
        }
    }
}
```

```groovy
@Test
void exampleReadFileTest() {
    helper.addFileExistsMock('output', true)
    helper.addReadFileMock('output', 'FAILED!!!')

    runScript('Jenkinsfile')

    assertJobStatusFailure()
}
```

### Mocking Shell Steps

The shell steps (`sh`, `bat`, etc) are used by many pipelines for a variety of tasks.
They can be mocked to either (a) statically return:

- A string for standard output
- A return code

Or (b), to execute a closure that returns a `Map` (with `stdout` and `exitValue` entries).
The closure will be executed when the shell is called, allowing for dynamic behavior.

Here is a sample pipeline and corresponding unit tests for each of these variants.

```groovy
// Jenkinsfile
node {
    stage('Mock build') {
        String systemType = sh(returnStdout: true, script: 'uname')
        if (systemType == 'Debian') {
            sh './build.sh --release'
            int status = sh(returnStatus: true, script: './test.sh')
            if (status > 0) {
                currentBuild.result = 'UNSTABLE'
            } else {
                def result = sh(
                    returnStdout: true,
                    script: './processTestResults.sh --platform debian',
                )
                if (!result.endsWith('SUCCESS')) {
                    currentBuild.result = 'FAILURE'
                    error 'Build failed!'
                }
            }
        }
    }
}
```

```groovy
@Test
void debianBuildSuccess() {
    helper.addShMock('uname', 'Debian', 0)
    helper.addShMock('./build.sh --release', '', 0)
    helper.addShMock('./test.sh', '', 0)
    // Have the sh mock execute the closure when the corresponding script is run:
    helper.addShMock('./processTestResults.sh --platform debian') { script ->
        // Do something "dynamically" first...
        return [stdout: "Executing ${script}: SUCCESS", exitValue: 0]
    }

    runScript("Jenkinsfile")

    assertJobStatusSuccess()
}

@Test
void debianBuildUnstable() {
    helper.addShMock('uname', 'Debian', 0)
    helper.addShMock('./build.sh --release', '', 0)
    helper.addShMock('./test.sh', '', 1)

    runScript('Jenkinsfile')

    assertJobStatusUnstable()
}
```

Note that in all cases, the `script` executed by `sh` must *exactly* match the string
passed to `helper.addShMock`, including the script arguments, whitespace, etc. For more
flexible matching, you can use a pattern (regular expression) and even capture groups:

```groovy
helper.addShMock(~/.\/build.sh\s--(.*)/) { String script, String arg ->
    assert (arg == 'debug') || (arg == 'release')
    return [stdout: '', exitValue: 2]
}
```

Also, mocks are stacked, so if two mocks match a call, the last one wins. Combined with a
match-everything mock, you can tighten your tests a bit:

```groovy
@BeforeEach
void setUp() {
    super.setUp()
    helper = new PipelineTestHelper()
    // Basic `sh` mock setup:
    // - generate an error on unexpected calls
    // - ignore any echo (debug) outputs, they are not relevant
    // - all further shell mocks are configured in the test
    helper.addShMock() { throw new Exception('Unexpected sh call') }
    helper.addShMock(~/echo\s.*/, '', 0)
}
```

### Analyzing the Mock Execution

The helper registers every method call to provide a stacktrace of the mock execution.

```groovy
@Test
void shouldExecuteWithoutErrors() {
    runScript('Jenkinsfile')

    assertJobStatusSuccess()
    assertThat(helper.callStack.findAll { call ->
        call.methodName == 'sh'
    }.any { call ->
        callArgsToString(call).contains('mvn verify')
    }).isTrue()
}
```

This will also check that `mvn verify` was called during the job execution.

### Checking Pipeline Status

Let's say you have a simple script, and you'd like to check its behavior if a step fails.

```groovy
// Jenkinsfile
node() {
    git 'some_repo_url'
    sh 'make'
}
```

You can mock the `sh` step to just update the pipeline status to `FAILURE`. To verify that
your pipeline is failing, you need to check the status with
`BasePipelineTest.assertJobStatusFailure()`.

```groovy
@Test
void checkBuildStatus() {
    helper.registerAllowedMethod('sh', [String]) { cmd ->
        if (cmd == 'make') {
            binding.getVariable('currentBuild').result = 'FAILURE'
        }
    }

    runScript('Jenkinsfile')

    assertJobStatusFailure()
}
```

### Checking Pipeline Exceptions

Sometimes it is useful to verify that a specific exception was thrown during the pipeline
run. JUnit 4 and 5 have slightly different mechanisms for doing this.

For both examples below, assume that the following pipeline is being tested:

To do so you can use `org.junit.rules.ExpectedException`

```groovy
// Jenkinsfile
node {
    throw new IllegalArgumentException('oh no!')
}
```

#### JUnit 4

```groovy
class TestCase extends BasePipelineTest {
    @Test(expected = IllegalArgumentException)
    void verifyException() {
        runScript('Jenkinsfile')
    }
}
```

#### JUnit 5
```groovy
import static org.junit.jupiter.api.Assertions.assertThrows

class TestCase extends BasePipelineTest {
    @Test
    void verifyException() {
        assertThrows(IllegalArgumentException) { runScript('Jenkinsfile') }
    }
}
```

### Compare the Callstack with a Previous Implementation

One other use of the callstacks is to check your pipeline executions for possible
regressions. You have a dedicated method you can call if you extend `BaseRegressionTest`:

```groovy
@Test
void testPipelineNonRegression() {
    loadScript('job/exampleJob.jenkins').execute()
    super.testNonRegression('example')
}
```

This will compare the current callstack of the job to the one you have in a text callstack
reference file. To update this file with new callstack, just set this JVM argument when
running your tests: `-Dpipeline.stack.write=true`. You then can go ahead and commit this
change in your SCM to check in the change.

### Preserve Original Callstack Argument References

The default behavior of the callstack capture is to clone each call's arguments to
preserve their values at time of the call should those arguments mutate downstream. That
is a good guard when your scripts are passing ordinary mutable variables as arguments.

However, argument types that are not `Cloneable` are captured as `String` values. Most of
the time this is a perfect fallback. But for some complex types, or for types that don't
implement `toString()`, it can be tricky or impossible to validate the call values in a
test.

Take the following simple example:

```groovy
Map pretendArgsFromFarUpstream = [
    foo: 'bar',
    foo2: 'more bar please',
    aNestedMap: [aa: 1, bb: 2],
    plusAList: [1, 2, 3, 4],
].asImmutable()

node() {
    doSomethingWithThis(pretendArgsFromFarUpstream)
}
```

`pretendArgsFromFarUpstream` is an immutable map and will be recorded as a `String` in the
callstack. Your test may want to perform fine-grained validations via map key referencing
instead of pattern matching or similar parsing. For example:

```groovy
assertEquals(2, arg.aNestedMap.bb)
```

You may want to perform this kind of validation, particularly if your pipelines pass
`final` and/or immutable variables as arguments. You can retain the direct reference to
the variable in the callstack by setting this switch in your test setup:

```groovy
helper.cloneArgsOnMethodCallRegistration = false
```

### Running Inline Scripts

In case you want to have some script executed directly within a test case rather than
creating a resource file for it, `loadInlineScript` and `runInlineScript` can be used.

```groovy
@Test
void testSomeScript() {
    Object script = loadInlineScript('''
        node {
            stage('Build') {
                sh 'make'
            }
        }
    ''')

    script.execute()

    printCallStack()
    assertJobStatusSuccess()
}
```

Note that inline scripts cannot be debugged via breakpoints as there is no file to attach
to!

## Configuration

The abstract class `BasePipelineTest` configures the helper with useful conventions:

- It looks for pipeline scripts in your project in root (`./.`) and `src/main/jenkins`
  paths.
- Jenkins pipelines let you load other scripts from a parent script with `load` command.
  However `load` takes the full path relative to the project root. The test helper mock
  successfully the `load` command to load the scripts. To make relative paths work, you
  need to configure the path of the project where your pipeline scripts are, which
  defaults to `.`.
- Pipeline script extension, which defaults to jenkins (matches any `*.jenkins` file)

Overriding these default values is easy:

```groovy
class TestExampleJob extends BasePipelineTest {
    @Override
    @BeforeEach
    void setUp() {
        baseScriptRoot = 'jenkinsJobs'
        scriptRoots += 'src/main/groovy'
        scriptExtension = 'pipeline'
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

## Declarative Pipelines

To test a declarative pipeline, you'll need to subclass the `DeclarativePipelineTest`
class instead of `BasePipelineTest`

```groovy
// Jenkinsfile
pipeline {
    agent none
    stages {
        stage('Example Build') {
            agent { docker 'maven:3-alpine' }
            steps {
                echo 'Hello, Maven'
                sh 'mvn --version'
            }
        }
        stage('Example Test') {
            agent { docker 'openjdk:8-jre' }
            steps {
                echo 'Hello, JDK'
                sh 'java -version'
            }
        }
    }
}
```

```groovy
import com.lesfurets.jenkins.unit.declarative.*

class TestExampleDeclarativeJob extends DeclarativePipelineTest {
    @Test
    void shouldExecuteWithoutErrors() {
        runScript("Jenkinsfile")

        assertJobStatusSuccess()
        printCallStack()
    }
}
```

### DeclarativePipelineTest

The DeclarativePipelineTest class extends `BasePipelineTest`, so you can verify your
declarative job the same way as scripted pipelines.

## Testing Pipelines That Use Shared Libraries

With [Shared Libraries](https://jenkins.io/doc/book/pipeline/shared-libraries/), Jenkins
lets you share common code from pipelines across different repositories. Shared libraries
are configured in the Jenkins settings and imported with `@Library` annotation or the
`library` step.

Testing pipeline scripts using external libraries is not trivial because the shared
library code is another repository. JenkinsPipelineUnit lets you test shared libraries and
pipelines that depend on these libraries.

Here is an example pipeline using a shared library:

```groovy
@Library('commons')

import net.courtanet.jenkins.Utils

sayHello 'World'

node() {
    stage ('Checkout') {
        def utils = new Utils()
        checkout "${utils.gitTools()}"
    }
    stage ('Build') {
        sh './gradlew build'
    }
    stage ('Post Build') {
        String json = libraryResource 'net/courtanet/jenkins/request.json'
        sh "curl -H 'Content-Type: application/json' -X POST -d '$json' ${acme.url}"
    }
}
```

This pipeline is using a shared library called `commons`. Now let's test it:

```groovy
// You need to import the class first
import static com.lesfurets.jenkins.unit.global.lib.LibraryConfiguration.library

class TestCase extends BasePipelineTest {
    @Test
    void testLibrary() {
        Object library = library()
            .name('commons')
            .retriever(gitSource('git@example.com:libs/commons.git'))
            .targetPath('path/to/clone')
            .defaultVersion("master")
            .allowOverride(true)
            .implicit(false)
            .build()
        helper.registerSharedLibrary(library)

        runScript('job/library/exampleJob.jenkins')

        printCallStack()
    }
}
```

Notice how the shared library is defined and registered to the helper. The library
definition is done via a fluent API which lets you set the same configurations as in
[Jenkins Global Pipeline
Libraries](https://jenkins.io/doc/book/pipeline/shared-libraries/#using-libraries).

The `retriever` and `targetPath` fields tell the framework how to fetch the sources of the
library, and to which local path. The framework comes with two naive but useful
retrievers, `gitSource` and `localSource`. You can write your own retriever by
implementing the `SourceRetriever` interface.

Note that properties `defaultVersion`, `allowOverride` and `implicit` are optional with
default values `master`, `true` and `false`.

Now if we execute this test, the framework will fetch the sources from the Git repository
and load classes, scripts, global variables and resources found in the library. The
callstack of this execution will look like the following:

```text
Loading shared library commons with version master
libraryJob.run()
  libraryJob.sayHello(World)
  sayHello.echo(Hello, World.)
  libraryJob.node(groovy.lang.Closure)
     libraryJob.stage(Checkout, groovy.lang.Closure)
        Utils.gitTools()
        libraryJob.checkout({branch=master})
     libraryJob.stage(Build, groovy.lang.Closure)
        libraryJob.sh(./gradlew build)
     libraryJob.stage(Post Build, groovy.lang.Closure)
        libraryJob.libraryResource(net/courtanet/jenkins/request.json)
        libraryJob.sh(curl -H 'Content-Type: application/json' -X POST -d '{"name" : "Ben"}' http://acme.com)
```

### Library Source Retrievers

There are a few types of `SourceRetriever` implementations in addition to the previously
described `GitSource` one.

#### ProjectSource Retriever

The `ProjectSource` retriever is useful if you write tests for the library itself. So it
lets you load the library files directly from the project root folder (where the `src`
and `vars` folders are located).

Then you can use `projectSource` to point to the location of the library files. Calling
`projectSource()` with no arguments will look for files in the project root. With
`.defaultVersion('<notNeeded>')`,  you can load it in pipelines using `commons@master` or
`commons@features` which would use the same repository.

```groovy
import static com.lesfurets.jenkins.unit.global.lib.ProjectSource.projectSource

class TestCase extends BasePipelineTest {
    @Override
    @BeforeEach
    void setUp() {
        super.setUp()
        Object library = library()
            .name('commons')
            .defaultVersion('<notNeeded>')
            .allowOverride(true)
            .implicit(true)
            .targetPath('<notNeeded>')
            .retriever(projectSource())
            .build()
        helper.registerSharedLibrary(library)
    }
}
```

#### LocalSource Retriever

The `LocalSource` retriever is useful if you want to verify how well your library
integrates with the pipelines. For example you may use pre-copied library files with
different versions.

```groovy
import static com.lesfurets.jenkins.unit.global.lib.LocalSource.localSource

class TestCase extends BasePipelineTest {
    @Override
    @BeforeEach
    void setUp() {
        super.setUp()
        Object library = library()
            .name('commons')
            .defaultVersion('master')
            .allowOverride(true)
            .implicit(false)
            .targetPath('<notNeeded>')
            .retriever(localSource('/var/tmp/'))
            .build()
        helper.registerSharedLibrary(library)
    }
}
```

In the above example, the retriever would assume that the library files are located at
`/var/tmp/commons@master`.

### Loading Libraries Dynamically

There is partial support for loading dynamic libraries. It doesn't implement all the
features, but it could be useful sometimes.

Pipeline example:

```groovy
Object commonsLib = library 'commons'

// Assume that `sayHello` is a singleton in the `commons` library
sayHello 'World'

// Create an instance of a class in the `commons` library
Object utils = net.courtanet.jenkins.Utils.new()
```

Test class example:

```groovy
@Test
void testDynamicLibrary() {
    Object library = library()
        .name('commons')
        .retriever(gitSource('git@example.com:libs/commons.git'))
        .targetPath('path/to/clone')
        .defaultVersion('master')
        .allowOverride(true)
        .implicit(false)
        .build()
    helper.registerSharedLibrary(library)
    // Registration for pipeline method 'library' must be made after registering the
    // shared library. Unfortunately, this cannot be moved to the super class.
    helper.registerAllowedMethod('library', [String], { String name ->
        helper.getLibLoader().loadLibrary(name)
        println helper.getLibLoader().libRecords
        return new LibClassLoader(helper, null)
    })

    loadScript('job/library/exampleJob.jenkins')

    printCallStack()
}
```

### Library Global Variables with Library Object Arguments


You might have a library that defines global variables with library class instances as
arguments. For example, consider the following library class and global variable:

```groovy
// src/com/example/Monster.groovy
package com.example

class Monster {
    String moniker

    Monster(String moniker) {
      this.moniker = moniker
    }
}
```

```groovy
// vars/monster.groovy
import com.example.Monster

void call(Monster monster) {
    println "${monster.moniker} is always very scary"
}
```

Your pipeline uses both as follows:

```groovy
Monster vampire = new Monster('Dracula')
monster(vampire)
// Should print "Dracula is always very scary"
```

If this does not yield the expected output but instead throws a `MissingMethodException`
with the cause `No signature of method: Jenkinsfile.monster() is applicable for argument
types: (org.test.Monster) values: [com.example.Monster1@d34db33f]` you may need to disable
library class preloading in your test setup, which you do with the following switch.

```groovy
helper.libLoader.preloadLibraryClasses = false
```

You may need to do this on a test-by-test basis, as disabling class preloading can cause
problems in other cases. For example, when you have library classes that require access to
the `env` global.

## Writing Testable Libraries

We recommend the following best-practices for organizing pipeline code:

* Keep complex logic in the `Jenkinsfile` to a minimum
  - When possible, move complexity to external scripts that the `Jenkinsfile` executes
  - Move shared functionality to [pipeline libraries](https://www.jenkins.io/doc/book/pipeline/shared-libraries/)
  - Likewise, any tricky Groovy logic that can't be easily moved to external scripts
    should also be placed in pipeline libraries
* In pipeline libraries, organize logic in classes under `src`
  - Ideally, JenkinsPipelineUnit is used to test *only* these classes
* Use the `vars` singletons to instantiate classes from `src`

### On External Scripts

In general, it's better to avoid having complex build logic inside of build pipelines.
Although tools like `JenkinsPipelineUnit` are useful in testing pipelines, it's much
easier to run build scripts locally (meaning, outside of a Jenkins environment). Languages
like Python have much more sophisticated linting and testing tools than Groovy does.

That said, [CodeNarc](https://codenarc.org/) can be used to lint Groovy code, including
`Jenkinsfile` files.

### On Pipeline Library Organization

We recommend organizing pipeline libraries such that the bulk of the logic is organized
into classes, and the singletons being thin wrappers around these classes. This approach
has several advantages:

* It makes it easier to use OOP practices to organize the code
* It solves the problem of having to mock singletons inside of other singletons for tests
* It forces the script context to be injected into the class, which means less mocking of
  `@Library` calls and such

#### Example Pipeline Library Organization

Let's say we have a library responsible for a very complex operation, in this case, adding
two numbers together. 😄 Here's what that library (let's call it `HardMath`) might look
like:

```groovy
// src/com/example/HardMath.groovy
package com.example

class HardMath implements Serializable {
  // Jenkinsfile script context, note that all pipeline steps must use this context
  Object script = null

  int complexOperation(int a, int b) {
    // Note the script context is required for `echo`, as it is a pipeline step
    script.echo "Adding ${a} to ${b}"
    return a + b
  }
}
```

```groovy
// vars/hardmath.groovy
import com.example.HardMath

int complexOperation(int a, int b) {
  return new HardMath(script: this).complexOperation(a, b)
}
```

```groovy
// test/com/example/HardMathTest.groovy
package com.example

import static org.junit.jupiter.api.Assertions.assertEquals

import com.lesfurets.jenkins.unit.BasePipelineTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test


class HardMathTest extends BasePipelineTest {
  Object script = null

  @Override
  @BeforeEach
  void setUp() {
    super.setUp()
    this.script = loadScript('test/resources/EmptyPipeline.groovy')
  }

  @Test
  void testComplexOperation() {
    int result = new HardMath(script: script).complexOperation(1, 3)
    assertEquals(4, result)
  }
}
```

```groovy
// test/resources/EmptyPipeline.groovy
return this
```

And finally, in some other project's `Jenkinsfile`:

```groovy
// Jenkinsfile
@Library('hardmath')

node {
  stage('Hard Math') {
    int result = hardmath.complexOperation(5, 6)
    echo "The result is ${result}"
  }
}
```

For a larger real-world example of a pipeline library organized like the above and tested
with JenkinsPipelineUnit, have a look at
[`python-pipeline-utils`](https://github.com/Ableton/python-pipeline-utils/).

## Note on CPS

If you already fiddled with Jenkins pipeline DSL, you may have experienced strange errors
during execution on Jenkins. This is because Jenkins does not directly execute your
pipeline in Groovy, but transforms the pipeline code into an intermediate format in order
to run Groovy code in [Continuation Passing
Style](https://en.wikipedia.org/wiki/Continuation-passing_style) (CPS).

The usual errors are partly due to the [the sandboxing Jenkins
applies](https://wiki.jenkins-ci.org/display/JENKINS/Script+Security+Plugin#ScriptSecurityPlugin-GroovySandboxing)
for security reasons, and partly due to the [serializability Jenkins
imposes](https://github.com/jenkinsci/pipeline-plugin/blob/master/TUTORIAL.md#serializing-local-variables).

Jenkins requires that at each execution step, the whole script context is serializable, in
order to stop and resume the job execution. To simulate this aspect, CPS versions of the
helpers transform your scripts into the CPS format and check if at each step your script
context is serializable.

To use this _*experimental*_ feature, you can use the abstract class `BasePipelineTestCPS`
instead of `BasePipelineTest`. You may see some changes in the callstacks that the helper
registers. Note also that the serialization used to test is not the same as what Jenkins
uses. You may find some incoherence in that respect.

## Contributing

JenkinsPipelineUnit aims to help developers code and test Jenkins pipelines with a shorter
development cycle. It addresses some of the requirements traced in
[JENKINS-33925](https://issues.jenkins-ci.org/browse/JENKINS-33925). If you are willing to
contribute please don't hesitate to discuss in issues and open a pull-request.

## Demos and Examples

| URL | Frameworks and Tools | Test Subject | Test Layers |
|-----|----------------------|--------------|-------------|
| https://github.com/macg33zr/pipelineUnit | Spock, Gradle(Groovy)  | Jenkinsfile, scripted pipeline, SharedLibrary | UnitTest |
| https://github.com/mkobit/jenkins-pipeline-shared-library-example | Spock, Gradle (Kotlin), Junit | SharedLibrary | Integration, Unit|
| https://github.com/stchar/pipeline-sharedlib-testharness          | Junit, Gradle(Groovy) | SharedLibrary | Integration, Unit |
| https://github.com/stchar/pipeline-dsl-seed                       | Junit, Spock, Gradle(Groovy) | scripted pipeline | Integration(jobdsl), Unit |
| https://github.com/SpencerMalone/JenkinsPipelineIntegration       | Spock, Gradle(Groovy) | SharedLibrary | Integration |
| https://github.com/venosov/jenkins-pipeline-shared-library-example-victor       | Junit, Gradle(Kotlin) | SharedLibrary | Unit |
