package com.lesfurets.jenkins.unit.global.lib

import org.codehaus.groovy.ast.ClassNode
import org.codehaus.groovy.ast.MethodNode
import org.codehaus.groovy.classgen.GeneratorContext
import org.codehaus.groovy.control.SourceUnit

import com.cloudbees.groovy.cps.CpsTransformer

/**
 * Skips CPS transformation for library loaded classes
 */
class LibraryCpsTransformer extends CpsTransformer {

    boolean skipTransform = false

    private static LibraryAnnotationTransformer findAnnotationCustomizer(GeneratorContext context) {
        def customizer = context.compileUnit.config.compilationCustomizers.find {
            it.class == LibraryAnnotationTransformer
        }
        return LibraryAnnotationTransformer.cast(customizer)
    }

    @Override
    void call(SourceUnit source, GeneratorContext context, ClassNode classNode) {
        boolean f = false
        LibraryAnnotationTransformer annotationTransformer = findAnnotationCustomizer(context)
        if (annotationTransformer) {
            def records = annotationTransformer.getLibraryRecords()
            records.stream()
                    .flatMap { it.rootPaths.stream() }
                    .forEach {
                if (source.name.startsWith(it)) {
                    f = true
                }
            }
        }
        skipTransform = f
        super.call(source, context, classNode)
        skipTransform = false
    }

    @Override
    protected boolean shouldBeTransformed(MethodNode node) {
        return !skipTransform && super.shouldBeTransformed(node)
    }
}
