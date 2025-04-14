import bindgen.interface.Binding
import org.scalajs.linker.interface.OutputPatterns
import scala.scalanative.build.{ BuildTarget, GC, LTO, Mode }

ThisBuild / scalaVersion := "3.6.4"
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

lazy val os = System.getProperty("os.name").toLowerCase

lazy val nativePackageName = "libdap"

/* Distributed Asynchronous Petri Nets (DAP) library subproject. */
lazy val dap = crossProject(JVMPlatform, NativePlatform, JSPlatform)
  .crossType(CrossType.Full)
  .in(file("dap"))
  .nativeConfigure(
    _.settings(
      nativeConfig ~= { defaultConf =>
        // Macos builds differently from linux,
        // see https://stackoverflow.com/questions/66268814/dyld-library-not-loaded-how-to-correctly-tell-gcc-compiler-where-to-find/66284977#66284977]
        val additionalLinkingOptions =
          if (os contains "mac") Seq(s"-Wl,-install_name,'@rpath/$nativePackageName.dylib'") else Seq()
        defaultConf.withGC(GC.boehm) // garbage collector
          .withLTO(LTO.full) // link-time optimization
          .withMode(Mode.releaseSize) // build mode
          .withBuildTarget(BuildTarget.libraryDynamic) // build target: dynamic library, static library, executable
          .withLinkingOptions(defaultConf.linkingOptions ++ additionalLinkingOptions)
      },
      bindgenBindings := Seq(
        Binding(header = (Compile / resourceDirectory).value / "dap.h", nativePackageName).withExport(true),
      )
    ).enablePlugins(BindgenPlugin)
  )
  .jsConfigure(
    _.settings(
      scalaJSLinkerConfig ~= {
        _.withModuleKind(ModuleKind.ESModule)
          .withOutputPatterns(OutputPatterns.fromJSFile("%s.mjs"))
      },
    )
  )
  .settings(
    name := "dap",
    libraryDependencies ++= Seq(
      "org.scalatest" %%% "scalatest" % "3.2.19",
      "com.outr" %%% "scribe" % "3.16.0",
    ),
  )

/* Subprojects for DAP library's client with some example using JVM platform. */
lazy val dapJVMExamples = project.in(file("dap-jvm-examples"))
  .settings(
    name := "dap-jvm-examples",
    libraryDependencies ++= Seq(
      "org.scalatest" %% "scalatest" % "3.2.19"
    ),
    Compile / mainClass := Some("it.unibo.dap.examples.GossipSimulationApp")
  )
  .dependsOn(dap.jvm)
