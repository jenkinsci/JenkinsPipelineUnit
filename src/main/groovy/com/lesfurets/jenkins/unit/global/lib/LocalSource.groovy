package com.lesfurets.jenkins.unit.global.lib

import groovy.transform.CompileStatic
import groovy.transform.Immutable

@Immutable
@CompileStatic
class LocalSource implements SourceRetriever {

    String sourceURL

    @Override
    List<URL> retrieve(String repository, String branch, String targetPath) {
        def sourceDir = new File(sourceURL).toPath().resolve("$repository@$branch").toFile()
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
