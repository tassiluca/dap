import bindgen.interface.Binding

import scala.scalanative.build.{BuildTarget, GC, LTO, Mode}

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
  "-language:implicitConversions"
)

/* Distributed Asynchronous Petri Nets (DAP) library subproject. */
lazy val dap = crossProject(JVMPlatform, NativePlatform)
  .crossType(CrossType.Full)
  .in(file("dap"))
  .nativeConfigure(
    _.settings(
      nativeConfig ~= { conf =>
        conf.withGC(GC.boehm) // garbage collector
          .withLTO(LTO.full) // link-time optimization
          .withMode(Mode.releaseSize) // build mode
          .withLinkingOptions(Seq()) // a sequence of additional linker options to be passed to clang
          .withBuildTarget(BuildTarget.libraryDynamic) // build target: dynamic library, static library, executable
      },
      bindgenBindings := Seq(
        Binding(header = (Compile / resourceDirectory).value / "dap.h", packageName = "libdap")
          .withExport(true),
      )
    ).enablePlugins(BindgenPlugin)
  )
  .settings(
    name := "dap",
    libraryDependencies ++= Seq(
      "org.scalatest" %%% "scalatest" % "3.2.19",
      "ch.epfl.lamp" %%% "gears" % "0.2.0",
      "com.outr" %%% "scribe" % "3.16.0",
    )
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

/* Subproject for DAP library's client with some example using the Native platform. */
lazy val dapNativeExamples = project.in(file("dap-native-examples"))
  .settings(
    name := "dap-native-examples",
  )
  .dependsOn(dap.native)
