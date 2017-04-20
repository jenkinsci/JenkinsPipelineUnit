package com.lesfurets.jenkins.unit

import static org.codehaus.groovy.runtime.MetaClassHelper.isAssignableFrom

import groovy.transform.CompileStatic

@CompileStatic
class MethodSignature {
    String name
    Class[] args

    static MethodSignature method(String name, Class... args = []) {
        return new MethodSignature(name, args)
    }

    MethodSignature(String name, Class[] args) {
        this.name = name
        this.args = args
    }

    String argsToString() {
        return args.collect { Class it ->
            if (it != null && Closure.isAssignableFrom(it)) {
                Closure.class.toString()
            } else {
                String.valueOf(it)
            }
        }.join(', ')
    }

    boolean equals(o) {
        if (this.is(o)) return true
        if (getClass() != o.class) return false

        MethodSignature that = (MethodSignature) o

        if (name != that.name) return false
        if (args == null && that.args == null) return true
        if (args.size() != that.args.size()) return false
        for (int i = 0; i < args.size(); i++) {
            Class thisClazz = this.args[i]
            Class thatClazz = that.args[i]
            if (!(isAssignableFrom(Closure.class, thatClazz) && isAssignableFrom(Closure.class, thisClazz))) {
                if (!isAssignableFrom(thisClazz, thatClazz)) {
                    return false
                }
            }
        }
        return true
    }

    int hashCode() {
        int result
        result = (name != null ? name.hashCode() : 0)
        result = 31 * result + (args != null ? argsToString().hashCode() : 0)
        return result
    }


    @Override
    String toString() {
        return "MethodSignature{ name='$name', args=${Arrays.toString(args)} }"
    }
}
