package com.lesfurets.jenkins.unit

import org.apache.commons.lang3.reflect.ConstructorUtils;
import org.apache.commons.lang3.reflect.MethodUtils;

/**
 * Kind of proxy object to create instances of library's classes
 *
 * Copied from https://github.com/jenkinsci/workflow-cps-global-lib-plugin/blob/master/src/main/java/org/jenkinsci/plugins/workflow/libs/LibraryStep.java
 */
class LibClassLoader extends GroovyObjectSupport {
  private String className
  private PipelineTestHelper helper
  Class loadedClass

  LibClassLoader(helper, String className){
    this.helper = helper
    this.className = className
    this.loadedClass = null
  }

  LibClassLoader(helper, String className, loadedClass){
    this.helper = helper
    this.className = className
    this.loadedClass = loadedClass
  }

  @Override
  Object getProperty(String property) {

    if(loadedClass) {
      return loadedClass.getProperties()[property]
    }

    if(!this.className) {
      return new LibClassLoader(this.helper, property)
    }

    if(property =~ /^[A-Z].*/) {

      def gcl = this.helper.getLibLoader().getGroovyClassLoader()
      loadedClass = gcl.loadClass( (String) "${this.className}.${property}")
      return new LibClassLoader(this.helper, "${this.className}.${property}", loadedClass)
    } else {
      return new LibClassLoader(this.helper, "${this.className}.${property}")
    }
  }

  @Override
  Object invokeMethod(String name, Object _args) {
    Object[] args = _args as Object[]
    if(loadedClass) {
      if (name.equals("new")) {
        return ConstructorUtils.invokeConstructor(loadedClass, args);
      } else {
        return MethodUtils.invokeStaticMethod(loadedClass, name, args);
      }
    }

  }
}