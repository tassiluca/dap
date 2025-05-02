import com.pswidersk.gradle.python.VenvTask
import java.nio.file.Path

val libFolder = rootDir.resolve("lib")
val libDAP = DAPNativeLib()

class DAPNativeLib {
    val name: String = "dap"
    val libraryName = "lib$name"
    val project: File = rootDir.parentFile.resolve(name)

    fun headerFile(): File? = matchingFile { it.name == "$name.h" }

    fun libFile(): File? = matchingFile { it.name.matches(Regex("$libraryName\\.(so|dylib|dll|lib)")) }

    private fun matchingFile(predicate: (File) -> Boolean) = project.walkTopDown().find { it.isFile && predicate(it) }

    fun build(): File? = exec {
        workingDir(rootDir.parentFile)
        commandLine("sbt", "clean", "${name}Native/nativeLink")
    }.assertNormalExitValue().let { libFile() }
}

plugins {
    alias(libs.plugins.c.application)
    alias(libs.plugins.python.application)
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

/* Python configuration. */
pythonPlugin {
    pythonVersion = "3.13.1"
}

val pipInstall by tasks.registering(VenvTask::class) {
    venvExec = "pip"
    val wheelFile = libFolder.walkTopDown().find { it.extension == "whl" } ?: error("No wheel found in $libFolder")
    args = listOf("install", "$wheelFile", "--isolated", "-r", "requirements.txt")
}

tasks.register<VenvTask>("runPython") {
    workingDir = projectDir.resolve("src/main/python")
    val scriptArgs = project.findProperty("args") as String? ?: error("No args provided")
    args = listOf("-u", "gossip.py") + scriptArgs.split("\\s+".toRegex())
    dependsOn(pipInstall)
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
