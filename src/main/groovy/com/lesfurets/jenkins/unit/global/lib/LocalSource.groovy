package com.lesfurets.jenkins.unit.global.lib

import groovy.transform.CompileStatic
import groovy.transform.Immutable

@Immutable
@CompileStatic
class LocalSource implements SourceRetriever {

    String sourceURL

    @Override
    List<URL> retrieve(String repository, String branch, String targetPath) {
        def sourceURLPath = new File(sourceURL).toPath()
        def sourceDir
        if (branch) {
            sourceDir = sourceURLPath.resolve("$repository@$branch").toFile()
        } else {
            sourceDir = sourceURLPath.resolve(repository).toFile()
        }

        if (sourceDir.exists()) {
            return [sourceDir.toURI().toURL()]
        }
        throw new IllegalStateException("Directory $sourceDir.path does not exists")
    }

    static LocalSource localSource(String source) {
        new LocalSource(source)
    }

    @Override
    String toString() {
        return "LocalSource{" +
                        "sourceURL='" + sourceURL + '\'' +
                        '}'
    }
}
