import java.nio.file.Path

plugins {
    id("dev.guillermo.gradle.c-application") version "0.5.0"
}

application {
    privateHeaders.from(file("src/main/c"), libFolder)
    dependencies {
        implementation(files(libFolder))
    }
}

tasks.withType<LinkExecutable>().configureEach {
    linkerArgs.addAll(listOf("-L${libFolder.absolutePath}", "-ldap", "-Wl,-rpath,${libFolder.absolutePath}"))
}

val libFolder = file("lib").also { it.mkdir() }
inner class DAPNativeLib {
    val name: String = "dap"
    val libraryName = "lib$name"
    val projectPath: Path = rootDir.parentFile.toPath().resolve(name)
    fun headerFile(): File? = projectPath.toFile()
        .walkTopDown()
        .find { it.isFile && it.name == "$name.h" }
    fun libFile(): File? = projectPath.toFile()
        .walkTopDown()
        .find { it.isFile && it.name.matches(Regex("$libraryName\\.(so|dylib|dll|lib)")) }
    fun build(): File? = exec {
        workingDir(rootDir.parentFile)
        commandLine("sbt", "dapNative/nativeLink")
    }.assertNormalExitValue().let { libFile() }
}
val libdap = DAPNativeLib()

tasks.register("buildLibs") {
    group = "build"
    description = "Build needed libraries"
    doLast {
        if (!libFolder.exists() || libFolder.listFiles()?.isEmpty() == true) {
            logger.quiet("Building DAP Native library...")
            libdap.build()?.let { builtLib ->
                builtLib.copyTo(File(libFolder, builtLib.name))
                libdap.headerFile()?.copyTo(File(libFolder, "${libdap.name}.h"))
            }
        }
    }
    mustRunAfter(tasks.clean)
}

tasks.clean.get().doLast {
    libFolder.deleteRecursively()
}
