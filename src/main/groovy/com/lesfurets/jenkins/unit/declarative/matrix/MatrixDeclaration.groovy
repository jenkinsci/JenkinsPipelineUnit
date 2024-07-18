package com.lesfurets.jenkins.unit.declarative.matrix

import com.lesfurets.jenkins.unit.declarative.GenericPipelineDeclaration

import static groovy.lang.Closure.DELEGATE_FIRST

class MatrixDeclaration extends GenericPipelineDeclaration {

    AxesDeclaration axes

    def axes(@DelegatesTo(strategy = DELEGATE_FIRST, value = AxesDeclaration) Closure closure) {
        this.axes = createComponent(AxesDeclaration, closure)
    }

    def execute(Object delegate) {
        super.execute(delegate)

        axes.axis.each {
            it.name
            it.values
        }


        axes.axis.combinations()
        def matrixAxes = getMatrixAxes()
        matrixAxes.each {
            this.stages.entrySet().forEach { e ->
                def savedEnv = [:]
                def env = e.value.binding.getVariable('env') as Map
                savedEnv.putAll(env)
                env.putAll(it)
                e.value.execute(delegate)
                e.value.binding.setVariable('env', savedEnv)
            }
        }
    }

    private List<Map> getMatrixAxes() {
        List calculatedAxes = []
        axes.axis.each { axis ->
            List axisList = []
            axis.values.each { value ->
                axisList << [(axis.name): value]
            }
            calculatedAxes << axisList
        }
        // calculate cartesian product
        calculatedAxes.combinations()*.sum()
    }

}
