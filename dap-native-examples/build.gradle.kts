import java.nio.file.Path

val libFolder = rootDir.resolve("lib")
val libDAP = DAPNativeLib()

inner class DAPNativeLib {
    val name: String = "dap"
    val libraryName = "lib$name"
    val projectPath: Path = rootDir.parentFile.toPath().resolve(name)

    fun headerFile(): File? = matchingFile { it.isFile && it.name == "$name.h" }

    fun libFile(): File? = matchingFile { it.isFile && it.name.matches(Regex("$libraryName\\.(so|dylib|dll|lib)")) }

    private fun matchingFile(predicate: (File) -> Boolean) = projectPath.toFile().walkTopDown().find(predicate)

    fun build(): File? = exec {
        workingDir(rootDir.parentFile)
        commandLine("sbt", "${name}Native/nativeLink")
    }.assertNormalExitValue().let { libFile() }
}

plugins {
    alias(libs.plugins.c.application)
}

/* C configuration. */
application {
    privateHeaders.from(file("src/main/c"), libFolder)
}

tasks.withType<CppCompile>().configureEach {
    dependsOn(buildNativeLibs)
}

tasks.withType<LinkExecutable>().configureEach {
    with(libFolder) {
        linkerArgs.addAll(listOf("-L$absolutePath", "-l${libDAP.name}", "-Wl,-rpath,$absolutePath"))
    }
}

/* Common configurations. */
tasks.build {
    dependsOn(buildNativeLibs)
}

tasks.clean {
    doLast { libFolder.deleteRecursively() }
}

val buildNativeLibs by tasks.registering {
    group = "build"
    description = "Build local native libraries"
    doLast {
        if (!libFolder.exists() || libFolder.listFiles()?.isEmpty() == true) {
            logger.quiet("Building DAP Native library...")
            libDAP.build()?.let { builtLib ->
                builtLib.copyTo(File(libFolder, builtLib.name))
                libDAP.headerFile()?.copyTo(File(libFolder, "${libDAP.name}.h"))
            }
        }
    }
}
