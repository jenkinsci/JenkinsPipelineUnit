package com.lesfurets.jenkins.unit.declarative


class ParametersDeclaration extends GenericPipelineDeclaration {

    void setParams(String name, Object val) {
        Map immutableParams = binding.getVariable('params') as Map
        if (immutableParams[name] == null) {
            Map mutableParams = [:]
            immutableParams.each { k, v -> mutableParams[k] = v }
            mutableParams[name] = val
            binding.setVariable('params', mutableParams.asImmutable())
        }
    }

    // dereference 'parameters closure
    def booleanParam(Map val) {
        this.setParams(val.name, val.defaultValue)
    }

    def choice(Map val) {
        this.setParams(val.name, val.choices[0])
    }

    def password(Map val) {
        this.setParams(val.name, val.defaultValue)
    }

    def string(Map val) {
        this.setParams(val.name, val.defaultValue)
    }

    def text(Map val) {
        this.setParams(val.name, val.defaultValue)
    }
}
