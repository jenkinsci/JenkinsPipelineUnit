# Release Procedure

## Pre-Requirements

In order to release a new version you need a `jenkins.io` account, push rights to this
repository and access to Jenkins' JFrog artifactory server. See the [Jenkins plugin
adoption procedure][jenkins-adopt-a-plugin].

## Overview

In short, the release procedure could described with the following steps:

1. Update the version in `gradle.properties` and create a new commit
2. Make a new tag
3. Build and publish artifacts to the [Jenkins plugin repository][jenkins-plugin-repo].
4. Update the version in `gradle.properties` for the next development cycle
5. Add release notes to GitHub

**NOTE:** repo.jenkins-ci.org might have a delay (1-2d) in publishing artifacts to public,
so mark new releases as `pre-release`. Once artifacts are available, the `pre-release`
label can be removed.

## Tools

To automate the above steps, JenkinsPipelineUnit uses a variety of Gradle plugins and
GitHub actions.

### Gradle plugins 

#### net.researchgate.release

This plugin is used to prepare new releases. This Gradle plugin automatically creates a
new commit with the updated release version, makes a new release tag, and then prepares
the repository for the next development cycle.

#### com.jfrog.artifactory

Needed to publish artifacts to the repo.jenkins-ci.org/releases maven repository.

### GitHub Actions

There is a [release-drafter][release-drafter] GitHub actions script to prepare release
notes based on issues and PRs which were made to the main branch since the last release.

## Example

Let's say current version is `1.6`, new version is `1.7`, and the next development version
is `1.8-SNAPSHOT`.

* Checkout release branch

```bash
git fetch
git checkout -B master origin/master
```

* Create release:

```
./gradlew release
# You will be asked for the new version, and new snapshot version
# (just press enter to use default values)

```
* Checkout release tag

```bash
git checkout v1.7
```

* Publish artifacts

```bash
# create  ~/.gradle/gradle.properties with the following content:
# artifactory_user=jenkins.io_username
# artifactory_password=s3cr3t

./gradlew artifactoryPublish 
```

* Publish new release notes at https://github.com/jenkinsci/JenkinsPipelineUnit/releases


[jenkins-adopt-a-plugin]: https://www.jenkins.io/doc/developer/plugin-governance/adopt-a-plugin/
[jenkins-plugin-repo]: https://repo.jenkins-ci.org/artifactory/releases/com/lesfurets/jenkins-pipeline-unit/
[release-drafter]: https://github.com/jenkinsci/.github/blob/master/.github/release-drafter.adoc
