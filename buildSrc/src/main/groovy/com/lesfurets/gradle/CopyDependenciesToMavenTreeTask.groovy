package com.lesfurets.gradle

import org.gradle.api.DefaultTask
import org.gradle.api.artifacts.Configuration
import org.gradle.api.tasks.Internal

import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction

class CopyDependenciesToMavenTreeTask extends DefaultTask {

    @Internal
    Configuration configuration

    def outputDir

    @OutputDirectory File getOutputDir() {
        project.file(outputDir)
    }

    @TaskAction
    def action() {
        configuration.resolvedConfiguration.resolvedArtifacts.each { artifact ->
            def g = artifact.moduleVersion.id.group.split(/\./).join('/')
            def a = artifact.moduleVersion.id.name
            def v = artifact.moduleVersion.id.version
            def path = "${getOutputDir()}/$g/$a/$v"
            project.copy {
                into path
                from artifact.file
            }
        }
        //return getOutputDir()
    }
}
