package com.lesfurets.jenkins.unit.global.lib

import java.util.concurrent.TimeUnit

import groovy.transform.CompileStatic
import groovy.transform.Immutable

@Immutable
@CompileStatic
class GitSource implements SourceRetriever {

    String sourceURL

    @Override
    List<URL> retrieve(String repository, String branch, String targetPath) throws IllegalStateException {
        File target = new File(targetPath)
        def fetch = target.toPath().resolve("$repository@$branch").toFile()
        if (fetch.exists()) {
            return [fetch.toURI().toURL()]
        } else {
            fetch.parentFile.mkdirs()
        }
        def command = "git clone -b $branch --single-branch $sourceURL $repository@$branch"
        println command
        def processBuilder = new ProcessBuilder(command.split(' '))
                        .inheritIO()
                        .directory(target)
        def proc = processBuilder.start()
        proc.waitFor(CLONE_TIMEOUT, TimeUnit.SECONDS)
        proc.exitValue()
        return [fetch.toURI().toURL()]
    }

    static GitSource gitSource(String source) {
        new GitSource(source)
    }

    @Override
    String toString() {
        return "GitSource{" +
                        "sourceURL='" + sourceURL + '\'' +
                        '}'
    }
}