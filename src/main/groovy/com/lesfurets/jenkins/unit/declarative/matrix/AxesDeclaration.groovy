package com.lesfurets.jenkins.unit.declarative.matrix

import com.lesfurets.jenkins.unit.declarative.GenericPipelineDeclaration
import groovy.transform.ToString

import static groovy.lang.Closure.DELEGATE_FIRST

@ToString(includePackage = false, includeNames = true, ignoreNulls = true)
class AxesDeclaration extends GenericPipelineDeclaration {
    List<AxisDeclaration> axis = []

    def axis(@DelegatesTo(strategy = DELEGATE_FIRST, value = AxisDeclaration) Closure closure) {
        this.axis.add( createComponent(AxisDeclaration, closure))
    }

}
