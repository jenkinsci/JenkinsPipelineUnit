# Release Procedure
## pre-requirements  
In order to release new version you need jenkins.io account, 
push rights to this repository and access to jenkins jfrog artifactory.
See [jenkins plugin adoption procedure](https://www.jenkins.io/doc/developer/plugin-governance/adopt-a-plugin/)

## Overview
In short release procedure could described in following steps:
1. Update version in `gradle.properties` and create new commit
2. Place a new tag
3. Build and publish artifacts to repo.jenkins.io/releases maven repository (jar, src, docs)
4. Update version in `gradle.properties` for next development cycle
5. Add new release notes to github

**NOTE:** repo.jenkins.io/releases has a delay (1-2d) in publishing artifacts to public so mark new release notes as `pre-release`
once artifacts are avalible `pre-release` label could be removed.

## Tools
To automate steps abobe JPU uses a vary of gradle plugins and github actions scitps

### Gradle plugins 
#### net.researchgate.release
JPU uses it to prepare new release.
This gralde plugin automativally cretaes new commit with updated release version,
places new release tag, and then prepare repository for the next development cycle.
#### com.jfrog.artifactory
Is needed to publish artifacts to repo.jenkins.io/releases maven repository.

### Github Actions
There is [release-drafter](https://github.com/jenkinsci/.github/blob/master/.github/release-drafter.adoc) github actions script to prepare release notes 
based on Issues and PR which were integrated with the mainline since previous release.

## Example
Let's say current version is 1.6, new version is 1.7, next development version is 1.8-SNAPSHOT

* checkout release branch
```bash
git fetch
git checkout -B master origin/master
```

* run release:
```
./gradlew release
# You will be asked for new version, and new snapsot version 
# (just press enter to use default values)

```
* checkout new tag
```bash
git checkout v1.7
```

* Publish artifacts
```bash
# create  ~/.gradle/gradle.properties with following content:
# artifactory_user=jenkins.io_username
# artifactory_password=s3cr3t

./gradlew artifactoryPublish 
```

* Publish new releas notes at https://github.com/jenkinsci/JenkinsPipelineUnit/releases

