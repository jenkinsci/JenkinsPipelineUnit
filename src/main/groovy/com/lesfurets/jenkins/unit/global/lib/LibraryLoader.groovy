package com.lesfurets.jenkins.unit.global.lib

/**
 * Loads libraries to groovy class loader
 */
class LibraryLoader {

    private final GroovyClassLoader groovyClassLoader

    private final Set<String> loadedLibraries = new HashSet<>()

    private final Map<String, LibraryConfiguration> libraryDescriptions

    LibraryLoader(GroovyClassLoader groovyClassLoader, Map<String, LibraryConfiguration> libraryDescriptions) {
        this.groovyClassLoader = groovyClassLoader
        this.libraryDescriptions = libraryDescriptions
    }

    /**
     * Loads all implicit library configurations
     */
    void loadImplicitLibraries() {
        libraryDescriptions.values().stream()
                        .filter { it.implicit }
                        .filter { !loadedLibraries.contains(getExpression(it)) }
                        .forEach {
            doLoadLibrary(it)
            loadedLibraries.add(getExpression(it))
        }
    }

    void loadLibrary(String expression) throws Exception {
        def lib = parse(expression)
        def libName = lib[0]
        def version = lib[1]
        def library = libraryDescriptions.get(libName)
        if (!library) {
            throw new Exception("Library description '$libName' not found")
        }
        if (!matches(libName, version, library)) {
            throw new Exception("Library '$expression' does not match description $library")
        }
        def loadedLib = getExpression(library, version)
        if (!loadedLibraries.contains(loadedLib)) {
            doLoadLibrary(library, version)
            loadedLibraries.add(loadedLib)
        }
    }

    /**
     * Loads library to groovy class loader.
     * TODO set global vars
     * @param library library configuration.
     * @param version version to load, if null loads the default version defined in configuration.
     * @throws Exception
     */
    private void doLoadLibrary(LibraryConfiguration library, String version = null) throws Exception {
        println "Loading shared library ${library.name} with version ${version ?: library.defaultVersion}"
        try {
            def urls = library.retriever.retrieve(library.name, version ?: library.defaultVersion, library.targetPath)
            urls.forEach { url ->
                def file = new File(url.toURI())
                groovyClassLoader.addURL(file.toPath().resolve('src').toUri().toURL())
            }
        } catch (Throwable t) {
            throw t
        }
    }

    private static String[] parse(String identifier) {
        identifier.split('@')
        int at = identifier.indexOf('@')
        if (at == -1) {
            return [identifier, null] // pick up defaultVersion
        } else {
            return [identifier.substring(0, at), identifier.substring(at + 1)]
        }
    }

    private static String getExpression(LibraryConfiguration lib, String version = null) {
        return "$lib.name@${version ?: lib.defaultVersion}"
    }

    private static boolean matches(String libName, String version, LibraryConfiguration libraryDescription) {
        if (libraryDescription.name == libName) {
            if (version == null) {
                return true
            }
            if (libraryDescription.allowOverride || libraryDescription.defaultVersion == version) {
                return true
            }
        }
        return false
    }

}
