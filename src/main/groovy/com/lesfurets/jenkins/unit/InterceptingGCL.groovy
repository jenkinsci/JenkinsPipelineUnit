package com.lesfurets.jenkins.unit

import org.codehaus.groovy.control.CompilationFailedException
import org.codehaus.groovy.control.CompilerConfiguration

class InterceptingGCL extends GroovyClassLoader {

    PipelineTestHelper helper
    InterceptingGCL(PipelineTestHelper helper,
                    ClassLoader loader,
                    CompilerConfiguration config) {
        super(loader, config)
        this.helper = helper
    }

    @Override
    Class parseClass(GroovyCodeSource codeSource, boolean shouldCacheSource)
                    throws CompilationFailedException {
        Class clazz = super.parseClass(codeSource, shouldCacheSource)
        clazz.metaClass.invokeMethod = helper.getMethodInterceptor()
        clazz.metaClass.static.invokeMethod = helper.getMethodInterceptor()
        clazz.metaClass.methodMissing = helper.getMethodMissingInterceptor()
        return clazz
    }

    @Override
    Class loadClass(final String name, boolean lookupScriptFiles, boolean preferClassOverScript, boolean resolve)
                throws ClassNotFoundException, CompilationFailedException {
        try {
            println "resolving class ${name}"
            return super.loadClass(name, lookupScriptFiles, preferClassOverScript, resolve)
        } catch (CompilationFailedException e) {
            println "$e.message"
            GString clazzBody = generateMockClass(name)
            println clazzBody
            return super.parseClass(clazzBody)
//            throw e
        } catch (ClassNotFoundException e) {
            if (name.contains('$')) {
                throw e
            } else {
                if (helper.mockedClasses.contains(name)) {
                    def clazzBody = generateMockClass(name)
                    println clazzBody
                    return super.parseClass(clazzBody)
                }
            }
//            else {
//                println "class not found $name"
//            }
        }
    }

    private GString generateMockClass(String name) {
        println "cannot find class ${name}, generating mock on-the-fly"
        def lastDot = name.lastIndexOf('.')
        def packageName = name.substring(0, lastDot)
        def className = name.substring(lastDot + 1, name.size())
        def clazzBody = """
package $packageName

import java.util.Map

class $className {

}
"""
        return clazzBody
    }


}