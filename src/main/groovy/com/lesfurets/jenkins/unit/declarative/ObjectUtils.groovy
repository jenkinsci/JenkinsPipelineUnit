package com.lesfurets.jenkins.unit.declarative

class ObjectUtils {

    static String printNonNullProperties(Object obj) {
        def props = obj.properties
        props.remove('class')
        obj.properties.entrySet().forEach { e ->
            if (e.value == null) {
                props.remove(e.key)
            }
        }
        return props.toString()
    }
}
