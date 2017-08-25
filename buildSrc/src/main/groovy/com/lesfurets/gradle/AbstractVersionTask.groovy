package com.lesfurets.gradle

import org.gradle.api.BuildCancelledException
import org.gradle.api.DefaultTask
import org.gradle.util.VersionNumber

class AbstractVersionTask extends DefaultTask {

    protected void writeVersion(String newVersion) {
        // verify version format
        def version = VersionNumber.parse(newVersion)
        if (version == VersionNumber.UNKNOWN) {
            throw new BuildCancelledException("Unknown version format: ${newVersion}")
        }
        if (version != project.version) {
            String contents = project.buildFile.getText("UTF-8")
            if (contents.find("version = \"${project.version}\"")) {

            }
            contents = contents.replaceFirst("version = \"${project.version}\"", "version = \"${newVersion}\"")
            project.version = newVersion
            project.buildFile.write(contents, "UTF-8")
        }
    }
}
