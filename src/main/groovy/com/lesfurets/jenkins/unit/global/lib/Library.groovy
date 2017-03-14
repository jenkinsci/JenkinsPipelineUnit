package com.lesfurets.jenkins.unit.global.lib

/**
 * Annotation definition to avoid missing import
 */
@interface Library {

    /**
     * Library names, each optionally followed by {@code @} and a version.
     */
    String[] value()

}