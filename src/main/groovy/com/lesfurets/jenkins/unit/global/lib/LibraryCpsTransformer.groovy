package com.lesfurets.jenkins.unit.global.lib

import org.codehaus.groovy.ast.ClassNode
import org.codehaus.groovy.ast.MethodNode
import org.codehaus.groovy.classgen.GeneratorContext
import org.codehaus.groovy.control.SourceUnit

import com.cloudbees.groovy.cps.CpsTransformer

class LibraryCpsTransformer extends CpsTransformer {

    boolean skipTransform = false

    @Override
    void call(SourceUnit source, GeneratorContext context, ClassNode classNode) {
        boolean f = source.name.contains('/vars/')
        skipTransform = f
        super.call(source, context, classNode)
        skipTransform = false
    }

    @Override
    protected boolean shouldBeTransformed(MethodNode node) {
        return !skipTransform && super.shouldBeTransformed(node)
    }
}
