package com.lesfurets.gradle

import org.gradle.api.internal.TaskInternal
import org.gradle.api.specs.Spec
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction

class SetVersionTask extends AbstractVersionTask {

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
        writeVersion(newVersion)
    }

}
