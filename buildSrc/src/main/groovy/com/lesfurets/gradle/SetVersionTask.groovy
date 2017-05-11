package com.lesfurets.gradle

import org.gradle.api.BuildCancelledException
import org.gradle.api.DefaultTask
import org.gradle.api.internal.TaskInternal
import org.gradle.api.specs.Spec
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction
import org.gradle.util.VersionNumber

class SetVersionTask extends DefaultTask {

    @Input
    String newVersion = project.properties['newVersion']

    @Override
    Spec<? super TaskInternal> getOnlyIf() {
        return {
            newVersion != project.version
        }
    }

    @TaskAction
    def action() {
        // verify version format
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
