package com.lesfurets.gradle

import org.gradle.api.DefaultTask
import org.gradle.api.internal.TaskInternal
import org.gradle.api.specs.Spec
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.TaskAction

class GitCommitTask extends DefaultTask {

    @Input
    @Optional
    String message = "Release v%s"

    @Override
    Spec<? super TaskInternal> getOnlyIf() {
        return {
            try {
                project.exec {
                    executable 'git'
                    args 'diff-index', '--quiet', 'HEAD', '--', "${project.buildFile.name}"
                }
            } catch (e) {
                return true
            }
            return false
        }
    }

    @TaskAction
    def action() {
        project.exec {
            executable 'git'
            args "commit", "-m", "${String.format(message, project.version)}", "--", "${project.buildFile.name}"
        }
    }

}
