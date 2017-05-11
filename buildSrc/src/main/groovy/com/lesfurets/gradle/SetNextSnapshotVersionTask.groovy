package com.lesfurets.gradle

import org.gradle.api.BuildCancelledException
import org.gradle.api.DefaultTask
import org.gradle.api.internal.TaskInternal
import org.gradle.api.specs.Spec
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction
import org.gradle.util.VersionNumber

class SetNextSnapshotVersionTask extends DefaultTask {


    public static final String SNAPSHOT = '-SNAPSHOT'

    @Override
    Spec<? super TaskInternal> getOnlyIf() {
        return {
            !project.version.toString().endsWith(SNAPSHOT)
        }
    }

    @TaskAction
    def action() {
        if (project.version.toString().endsWith(SNAPSHOT)) {
            throw new BuildCancelledException("Project version is already at snapshot : ${project.version}")
        }
        // verify next snapshot version format
        String newVersion = project.version + SNAPSHOT
        def version = VersionNumber.parse(newVersion)
        if (version == VersionNumber.UNKNOWN) {
            throw new BuildCancelledException("Unknown version format: ${newVersion}")
        }
        if (version != project.version) {
            String contents = project.buildFile.getText("UTF-8")
            contents = contents.replaceFirst("version = \"${project.version}\"", "version = \"${newVersion}\"")
            project.version = newVersion
            project.buildFile.write(contents, "UTF-8")
        }
    }

}
