package com.lesfurets.jenkins.unit.declarative.kubernetes


import groovy.transform.Memoized
import groovy.transform.ToString

import static com.lesfurets.jenkins.unit.declarative.ObjectUtils.printNonNullProperties
import static groovy.lang.Closure.DELEGATE_FIRST

@ToString(includePackage = false, includeNames = true, ignoreNulls = true)
class WorkspaceVolumeDeclaration {

    DynamicWorkspaceVolume dynamicPVC
    EmptyDirWorkspaceVolume emptyDirWorkspaceVolume
    HostPathWorkspaceVolume hostPathWorkspaceVolume
    NfsWorkspaceVolume nfsWorkspaceVolume
    PersistentVolumeClaimWorkspaceVolume persistentVolumeClaimWorkspaceVolume

    void dynamicPVC(@DelegatesTo(strategy = DELEGATE_FIRST, value = DynamicWorkspaceVolume) Closure closure) {
        this.dynamicPVC = createComponent(DynamicWorkspaceVolume, closure)
    }

    void emptyDirWorkspaceVolume(@DelegatesTo(strategy = DELEGATE_FIRST, value = EmptyDirWorkspaceVolume) Closure closure) {
        this.emptyDirWorkspaceVolume = createComponent(EmptyDirWorkspaceVolume, closure)
    }

    void hostPathWorkspaceVolume(@DelegatesTo(strategy = DELEGATE_FIRST, value = HostPathWorkspaceVolume) Closure closure) {
        this.hostPathWorkspaceVolume = createComponent(HostPathWorkspaceVolume, closure)
    }

    void nfsWorkspaceVolume(@DelegatesTo(strategy = DELEGATE_FIRST, value = NfsWorkspaceVolume) Closure closure) {
        nfsWorkspaceVolume = createComponent(NfsWorkspaceVolume, closure)
    }

    void persistentVolumeClaimWorkspaceVolume(@DelegatesTo(strategy = DELEGATE_FIRST, value = PersistentVolumeClaimWorkspaceVolume) Closure closure) {
        persistentVolumeClaimWorkspaceVolume = createComponent(PersistentVolumeClaimWorkspaceVolume, closure)
    }

    @Memoized
    String toString() {
        return printNonNullProperties(this)
    }

    @ToString(includePackage = false, includeNames = true, ignoreNulls = true)
    class DynamicWorkspaceVolume {
        String accessModes
        String requestsSize
        String storageClassName

        def accessModes(final String accessModes) {
            this.accessModes = accessModes
        }

        def requestsSize(final String requestsSize) {
            this.requestsSize = requestsSize
        }

        def storageClassName(final String storageClassName) {
            this.storageClassName = storageClassName
        }

        @Memoized
        String toString() {
            return printNonNullProperties(this)
        }
    }

    @ToString(includePackage = false, includeNames = true, ignoreNulls = true)
    class EmptyDirWorkspaceVolume {
        boolean memory

        def memory(final boolean memory) {
            this.memory = memory
        }
    }

    @ToString(includePackage = false, includeNames = true, ignoreNulls = true)
    class HostPathWorkspaceVolume {
        String hostPath

        def hostPath(final String hostPath) {
            this.hostPath = hostPath
        }

        @Memoized
        String toString() {
            return printNonNullProperties(this)
        }
    }

    @ToString(includePackage = false, includeNames = true, ignoreNulls = true)
    class NfsWorkspaceVolume {
        String serverAddress
        String serverPath
        boolean readOnly

        def serverAddress(final String serverAddress) {
            this.serverAddress = serverAddress
        }

        def serverPath(final String serverPath) {
            this.serverPath = serverPath
        }

        def readOnly(final boolean readOnly) {
            this.readOnly = readOnly
        }

        @Memoized
        String toString() {
            return printNonNullProperties(this)
        }
    }

    @ToString(includePackage = false, includeNames = true, ignoreNulls = true)
    class PersistentVolumeClaimWorkspaceVolume {
        String claimName
        boolean readOnly

        def claimName(final String claimName) {
            this.claimName = claimName
        }

        def readOnly(final boolean readOnly) {
            this.readOnly = readOnly
        }

        @Memoized
        String toString() {
            return printNonNullProperties(this)
        }
    }
}
