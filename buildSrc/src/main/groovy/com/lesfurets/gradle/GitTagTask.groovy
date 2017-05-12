package com.lesfurets.gradle

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.TaskAction

class GitTagTask extends DefaultTask {

    @Input
    @Optional
    String version

    @TaskAction
    def action() {
        project.exec {
            executable 'git'
            args 'tag', "--force", "v${version ?: project.version}"
        }
    }
}