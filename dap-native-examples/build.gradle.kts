import java.nio.file.Path

plugins {
    `cpp-application`
}

application {
    dependencies {

    }
}

val libFolder = file("lib").also { it.mkdir() }
inner class DAPNativeLib {
    val name: String = "dap"
    val libraryName = "lib$name"
    val projectPath: Path = rootDir.parentFile.toPath().resolve(name)
    fun libFile(): File? = projectPath.toFile()
        .walkTopDown()
        .find { it.isFile && it.name.matches(Regex("$libraryName\\.(so|dylib|dll|lib)")) }
    fun build(): File? = exec {
        workingDir(rootDir.parentFile)
        commandLine("sbt", "dapNative/nativeLink")
    }.assertNormalExitValue().let { libFile() }
}
val dapLib = DAPNativeLib()

tasks.build.get().dependsOn("buildLibs")

tasks.register("buildLibs") {
    group = "build"
    description = "Build needed libraries"
    doLast {
        if (libFolder.listFiles()?.isEmpty() == true) {
            println("Building DAP Native library...")
            val builtLib = dapLib.build()
            builtLib?.copyTo(File(libFolder, builtLib.name), overwrite = true)
        }
    }
}

tasks.clean.get().doLast {
    libFolder.deleteRecursively()
}
