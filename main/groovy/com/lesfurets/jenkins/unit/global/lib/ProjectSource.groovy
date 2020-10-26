package com.lesfurets.jenkins.unit.global.lib

import com.lesfurets.jenkins.unit.global.lib.SourceRetriever

import groovy.transform.CompileStatic
import groovy.transform.Immutable

/**
 * Retrieves the shared lib sources of the current project which are expected to be
 * at the default location (&quot;./vars&quot;), &quot;./src&quot;, &quot;./resources&quot;).
 * When working with this retriever the LibraryConfiguration which is provided
 * with this SourceRetrievers should be configured with <code>allowOverride=false<code>
 * since the ProjectSource retriever does not support loading of alternate versions. As
 * already outlined above this source retriever always loads the version as it is
 * contained at the default location.
 */

@Immutable
@CompileStatic
class ProjectSource implements SourceRetriever {

    String sourceURL

    /*
     * None of the parameters provided in the signature are used in the use-case of that retriever.
     */
    @Override
    List<URL> retrieve(String repository, String branch, String targetPath) {
        def sourceDir = new File(sourceURL)
        if (sourceDir.exists()) {
            return [sourceDir.getAbsoluteFile().toURI().toURL()]
        }
        throw new IllegalStateException("Directory $sourceDir.path does not exists")
    }

    static ProjectSource projectSource(String sourceDir = '.') {
        new ProjectSource(sourceDir)
    }

    @Override
    String toString() {
        return "${getClass().getSimpleName()}{" +
                        "sourceURL='" + sourceURL + '\'' +
                        '}'
    }
}
