# Release Procedure

## Pre-Requirements

In order to release a new version you need a `jenkins.io` account, push rights to this
repository and access to Jenkins' JFrog artifactory server. See [jenkins plugin adoption
procedure][jenkins-adopt-a-plugin].

## Overview

In short, the release procedure could described with the following steps:

1. Update the version in `gradle.properties` and create a new commit
2. Make a new tag
3. Build and publish artifacts to repo.jenkins.io/releases maven repository (jar, src,
   docs)
4. Update the version in `gradle.properties` for the next development cycle
5. Add release notes to GitHub

**NOTE:** repo.jenkins.io/releases has a delay (1-2d) in publishing artifacts to public,
so mark new releases as `pre-release`. Once artifacts are avalible, the `pre-release`
label can be removed.

## Tools

To automate the above steps, JPU uses a vary of gradle plugins and GitHub actions scripts.

### Gradle plugins 

#### net.researchgate.release

JPU uses this to prepare new releases. This Gradle plugin automativally creates a new
commit with the updated release version, makes a new release tag, and then prepares the
repository for the next development cycle.

#### com.jfrog.artifactory

Needed to publish artifacts to the repo.jenkins.io/releases maven repository.

### Github Actions

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
* Checkout new tag

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
[release-drafter]: https://github.com/jenkinsci/.github/blob/master/.github/release-drafter.adoc
