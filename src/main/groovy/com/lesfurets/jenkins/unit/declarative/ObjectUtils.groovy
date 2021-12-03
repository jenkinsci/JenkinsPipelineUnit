package com.lesfurets.jenkins.unit.declarative

class ObjectUtils {

    static String printNonNullProperties(Object obj) {
        def props = obj.properties.clone()
        props.remove('class')
        props.remove('binding')
        obj.properties.entrySet().forEach { e ->
            if (e.value == null) {
                props.remove(e.key)
            }
        }
        return props.toString()
    }
}
