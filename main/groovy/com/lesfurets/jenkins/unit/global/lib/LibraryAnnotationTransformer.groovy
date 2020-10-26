package com.lesfurets.jenkins.unit.global.lib

import org.codehaus.groovy.ast.AnnotatedNode
import org.codehaus.groovy.ast.AnnotationNode
import org.codehaus.groovy.ast.ClassCodeVisitorSupport
import org.codehaus.groovy.ast.ClassNode
import org.codehaus.groovy.ast.expr.ConstantExpression
import org.codehaus.groovy.ast.expr.Expression
import org.codehaus.groovy.ast.expr.ListExpression
import org.codehaus.groovy.classgen.GeneratorContext
import org.codehaus.groovy.control.CompilationFailedException
import org.codehaus.groovy.control.CompilePhase
import org.codehaus.groovy.control.SourceUnit
import org.codehaus.groovy.control.customizers.CompilationCustomizer
import org.codehaus.groovy.syntax.SyntaxException

import groovy.transform.CompileStatic

/**
 * Parses class definitions for Library annotation
 * Shamelessly adapted from workflow.cps.lib.plugin
 */
@CompileStatic
class LibraryAnnotationTransformer extends CompilationCustomizer {

    private final static String LIBRARY_ANNOTATION_CLASS_NAME = "Library"

    private final LibraryLoader libraryLoader

    LibraryAnnotationTransformer(LibraryLoader libraryLoader) {
        super(CompilePhase.CONVERSION)
        this.libraryLoader = libraryLoader
    }

    @Override
    void call(SourceUnit source, GeneratorContext context, ClassNode classNode) throws CompilationFailedException {
        final Map<String, AnnotationNode> libraryAnnotations = new HashMap<>()

        // load implicit libraries
        try {
            libraryLoader.loadImplicitLibraries()
        } catch (Exception e) {
            source.addException(e)
        }

        new ClassCodeVisitorSupport() {

            @Override
            protected SourceUnit getSourceUnit() {
                return source
            }

            @Override
            void visitAnnotations(AnnotatedNode node) {
                super.visitAnnotations(node)
                for (AnnotationNode annotationNode : node.getAnnotations()) {
                    String name = annotationNode.getClassNode().getName()
                    if (name == LIBRARY_ANNOTATION_CLASS_NAME) {
                        Expression value = annotationNode.getMember("value")
                        if (value instanceof ConstantExpression) { // one library
                            libraryAnnotations.put((String) ((ConstantExpression) value).getValue(), annotationNode)
                        } else { // several libraries
                            for (Expression element : ((ListExpression) value).getExpressions()) {
                                libraryAnnotations.put((String) ((ConstantExpression) element).getValue(), annotationNode)
                            }
                        }
                    }
                }
            }
        }.visitClass(classNode)

        // load libraries with error handling
        libraryAnnotations.entrySet().forEach { Map.Entry<String, AnnotationNode> it ->
            AnnotationNode annotation = it.value
            String expression = it.key
            try {
                libraryLoader.loadLibrary(expression)
            } catch (Exception e) {
                source.addError(new SyntaxException(e.getMessage(), annotation.lineNumber, annotation.columnNumber))
            }
        }

    }

}
