import bindgen.interface.Binding
import org.scalajs.linker.interface.OutputPatterns

import scala.scalanative.build.{ BuildTarget, GC, LTO, Mode }
import scala.sys.process.Process
import scala.util.chaining.scalaUtilChainingOps

ThisBuild / scalaVersion := "3.7.0"
ThisBuild / semanticdbEnabled := true
ThisBuild / semanticdbVersion := scalafixSemanticdb.revision
ThisBuild / scalacOptions ++= Seq(
  "-Wunused:all",
  "-Wvalue-discard",
  "-Wnonunit-statement",
  "-Wsafe-init",
  "-Ycheck-reentrant",
  "-Xcheck-macros",
  "-rewrite",
  "-indent",
  "-unchecked",
  "-explain",
  "-feature",
  "-language:implicitConversions",
  "-language:experimental.betterFors",
)

val libraryName = "dap"

/* Distributed Asynchronous Petri Nets (DAP) cross-project library. */
lazy val dap = crossProject(JVMPlatform, NativePlatform, JSPlatform)
  .crossType(CrossType.Full)
  .in(file("dap"))
  .settings(
    name := libraryName,
    libraryDependencies ++= Seq(
      "org.scalatest" %%% "scalatest" % "3.2.19",
      "com.outr" %%% "scribe" % "3.16.1",
      "org.scala-js" %% "scalajs-stubs" % "1.1.0" % "provided",
    ),
  )
  .nativeConfigure {
    val packageName = s"lib${libraryName}"
    _.settings(
      nativeConfig ~= { defaultConf =>
        // macOS requires an additional linking option to correctly set the runtime path of the dynamic 
        // library using the `-rpath` option. For more information, see https://stackoverflow.com/a/66284977.
        val additionalLinkOpts = if (os contains "mac") Seq(s"-Wl,-install_name,'@rpath/$packageName.dylib'") else Nil
        defaultConf.withGC(GC.boehm)
          .withLTO(LTO.full)
          .withMode(Mode.releaseSize)
          .withBuildTarget(BuildTarget.libraryDynamic)
          .withLinkingOptions(defaultConf.linkingOptions ++ additionalLinkOpts)
      },
    )
    //   bindgenBindings := Seq(
    //     Binding(header = (Compile / resourceDirectory).value / "dap.h", packageName).withExport(true),
    //   )
    // ).enablePlugins(BindgenPlugin)
  }
  .jsConfigure {
    _.settings(
      scalaJSLinkerConfig ~= {
        _.withModuleKind(ModuleKind.ESModule)
          .withOutputPatterns(OutputPatterns.fromJSFile("%s.mjs"))
      },
    )
  }

lazy val buildNativePythonWheel = taskKey[Unit]("Build the native Python wheel for the DAP library")
buildNativePythonWheel := {
  val log = streams.value.log
  val dapBaseDir = (dap.native / baseDirectory).value // dap/native
  val pythonSrcDir = dapBaseDir / "src" / "main" / "swig" / "bindings" / "python"
  val pythonTargetDir = dapBaseDir / "target" / "python-dist"
  // Build the native library
  val sharedLib = (dap.native / Compile / nativeLink).value
  val sharedLibHeader = dapBaseDir / "src" / "main" / "resources" / "dap.h"
  // Prepare environment
  if (pythonTargetDir.exists()) IO.delete(pythonTargetDir)
  IO.createDirectory(pythonTargetDir)
  IO.copyFile(sharedLib, pythonTargetDir / sharedLib.name, CopyOptions().withOverwrite(true))
  IO.copyFile(sharedLibHeader, pythonTargetDir / sharedLibHeader.name, CopyOptions().withOverwrite(true))
  IO.copyDirectory(pythonSrcDir, pythonTargetDir, CopyOptions().withOverwrite(true))
  // Build the wheel
  log.info("Building the native Python wheel...")
  val result = Process("python3 setup.py bdist_wheel", pythonTargetDir).!
  if (result != 0) sys.error("Failed to build the native Python wheel.") else log.success("Successfully built!")
}

lazy val os = System.getProperty("os.name").toLowerCase
