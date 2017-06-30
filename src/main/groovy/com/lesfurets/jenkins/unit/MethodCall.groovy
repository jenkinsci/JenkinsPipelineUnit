package com.lesfurets.jenkins.unit

import static com.lesfurets.jenkins.unit.MethodSignature.method

import org.codehaus.groovy.runtime.MetaClassHelper

import groovy.transform.CompileStatic

@CompileStatic
class MethodCall {

    Object target
    String methodName
    Object[] args
    int stackDepth

    /**
     * Extract method signature
     * @param call
     * @return method signature
     */
    static MethodSignature callToSignature(MethodCall call) {
        Class[] paramTypes = MetaClassHelper.castArgumentsToClassArray(call.args)
        return method(call.methodName, paramTypes)
    }

    /**
     * Extract joined string representation of call arguments.
     * @see #toStringWithClosures(java.lang.Object)
     * @param call
     * @return joined string representation of call arguments
     */
    static String callArgsToString(MethodCall call) {
        return call.args.collect { toStringWithClosures(it) }.join(', ')
    }

    /**
     * Convert argument object structure with Closures to String representation for comparison.
     * @param arg argument object
     * @return normalized String
     */
    static String toStringWithClosures(Object arg) {
        if (arg instanceof Closure) {
            return Closure.class.name
        } else if (arg instanceof Collection) {
            return arg.collect { k -> toStringWithClosures(k) }
        } else if (arg instanceof Map) {
            return ((Map) arg).collectEntries { k, v ->
                [k, v instanceof Closure ? Closure.class.name : toStringWithClosures(v)]
            }
        } else if (arg instanceof Script) {
            return "Script-${((Script) arg).class.name}"
        } else {
            return String.valueOf(arg)
        }
    }

    /**
     * @see #callToSignature(com.lesfurets.jenkins.unit.MethodCall)
     * @return method signature
     */
    MethodSignature toSignature() {
        return callToSignature(this)
    }

    /**
     * @see #callArgsToString(com.lesfurets.jenkins.unit.MethodCall)
     * @return string representation
     */
    String argsToString() {
        return callArgsToString(this)
    }

    @Override
    String toString() {
        return "${'   ' * (stackDepth)}" +
                        "${target instanceof Class ? target.simpleName : target.class.simpleName}." +
                        "$methodName(${argsToString()})"
    }

    boolean equals(o) {
        if (this.is(o)) return true
        if (getClass() != o.class) return false

        MethodCall that = (MethodCall) o

        if (stackDepth != that.stackDepth) return false
        // Probably incorrect - comparing Object[] arrays with Arrays.equals
        if (!Arrays.equals(args, that.args)) return false
        if (methodName != that.methodName) return false
        if (target != that.target) return false

        return true
    }

    int hashCode() {
        int result
        result = target.hashCode()
        result = 31 * result + methodName.hashCode()
        result = 31 * result + (args != null ? argsToString().hashCode() : 0)
        result = 31 * result + stackDepth
        return result
    }
}
