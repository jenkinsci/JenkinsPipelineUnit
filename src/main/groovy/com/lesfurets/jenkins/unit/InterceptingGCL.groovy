package com.lesfurets.jenkins.unit

import org.codehaus.groovy.control.CompilationFailedException
import org.codehaus.groovy.control.CompilerConfiguration
import org.codehaus.groovy.runtime.DefaultGroovyMethods


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
        def expando = new ExpandoMetaClass(clazz, true, true) {
            @Override
            Object invokeConstructor(Object[] arguments) {
                def mockedClass = getHelper().mockedClasses.get(theClass.name)
                if (mockedClass && arguments.size() != 0) {
                    def instance = DefaultGroovyMethods.newInstance(theClass)
                    instance.class = theClass
                    return mockedClass.initialize(instance, arguments)
//                    return instance
                }
                return super.invokeConstructor(arguments)
            }
        }
        expando.initialize()
        clazz.setMetaClass(expando)
//        clazz.metaClass.constructor = helper.getConstructorInterceptor()
        clazz.metaClass.invokeMethod = helper.getMethodInterceptor()
        clazz.metaClass.static.invokeMethod = helper.getMethodInterceptor()
        clazz.metaClass.methodMissing = helper.getMethodMissingInterceptor()
        return clazz
    }

    @Override
    Class loadClass(final String name, boolean lookupScriptFiles, boolean preferClassOverScript, boolean resolve)
                throws ClassNotFoundException, CompilationFailedException {
        try {
            return super.loadClass(name, lookupScriptFiles, preferClassOverScript, resolve)
        } catch (CompilationFailedException e) {
            GString clazzBody = generateMockClass(name)
            return super.parseClass(clazzBody)
        } catch (ClassNotFoundException e) {
            if (!name.contains('$')) {
                if (helper.mockedClasses.get(name)) {
                    def clazzBody = generateMockClass(name)
                    return super.parseClass(clazzBody)
                }
            }
            throw e
        }
    }

    private GString generateMockClass(String name) {
        def lastDot = name.lastIndexOf('.')
        def packageName = name.substring(0, lastDot)
        def className = name.substring(lastDot + 1, name.size())
        def clazzBody = """
package $packageName

class $className {

    def _properties = [:]
    def getProperty(String name) { _properties[name] }
    void setProperty(String name, value) { _properties[name] = value }

    void _setValues(def fieldName, def fieldVal) {setProperty(fieldName, fieldVal)}

}
"""
        return clazzBody
    }


}