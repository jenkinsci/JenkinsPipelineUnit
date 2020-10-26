package com.lesfurets.jenkins.unit.declarative


class ParametersDeclaration extends GenericPipelineDeclaration {

    void setParams(String key, Object val) {
        Map params = this.params
        if (params != null && (!params.containsKey(key))) {
            params[key] = val
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
