package com.lesfurets.gradle

import org.gradle.api.BuildCancelledException
import org.gradle.api.internal.TaskInternal
import org.gradle.api.specs.Spec
import org.gradle.api.tasks.TaskAction
import org.gradle.util.VersionNumber

class SetNextSnapshotVersionTask extends AbstractVersionTask {

    private static final String VERSION_TEMPLATE = "%d.%d%s"

    public static final String SNAPSHOT = 'SNAPSHOT'

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
        def currentVersion = VersionNumber.parse(project.version.toString())
        String newVersion = formatVersionWithoutMicro(incrementVersionWithSnapshot(currentVersion))
        writeVersion(newVersion)
    }

    VersionNumber incrementVersionWithSnapshot(VersionNumber versionNumber) {
        return new VersionNumber(versionNumber.major, versionNumber.minor+1, 0, SNAPSHOT)
    }

    String formatVersionWithoutMicro(VersionNumber versionNumber) {
        return String.format(VERSION_TEMPLATE, versionNumber.major, versionNumber.minor,
                        versionNumber.qualifier == null ? "" : "-" + versionNumber.qualifier)
    }

}
