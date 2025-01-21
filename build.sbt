import sbtcc.Executable
import scala.collection.immutable.ListSet
import scala.scalanative.build.{BuildTarget, GC, LTO, Mode}

ThisBuild / scalaVersion := "3.4.3"
ThisBuild / semanticdbEnabled := true
ThisBuild / semanticdbVersion := scalafixSemanticdb.revision

lazy val nativeCommonSettings = Seq(
  nativeConfig ~= {
    _.withGC(GC.default) // garbage collector
      .withLTO(LTO.full) // link-time optimization
      .withMode(Mode.releaseSize) // build mode
      .withLinkingOptions(Seq()) // a sequence of additional linker options to be passed to the native linker
      .withBuildTarget(BuildTarget.libraryDynamic) // build target: dynamic library, static library, executable
  },
)

/* Experiments subproject, used to experiment with Scala Native */
lazy val experiments = crossProject(JVMPlatform, NativePlatform)
  .crossType(CrossType.Full)
  .in(file("experiments"))
  .settings(
    name := "experiments",
    libraryDependencies ++= Seq(
      "org.scalatest" %%% "scalatest" % "3.2.19"
    )
  )
  .nativeSettings(nativeCommonSettings)

/* Distributed Asynchronous Petri Nets (DAP) library subproject. */
lazy val dap = crossProject(JVMPlatform, NativePlatform)
  .crossType(CrossType.Full)
  .in(file("dap"))
  .settings(
    name := "dap",
    libraryDependencies ++= Seq(
      "org.scalatest" %%% "scalatest" % "3.2.19"
    )
  )
  .nativeSettings(nativeCommonSettings)

/* Subprojects for DAP library's client with some example in both jvm and native. */
lazy val dapJVMExamples = project.in(file("dap-jvm-examples"))
  .settings(
    name := "dap-jvm-examples",
    libraryDependencies ++= Seq(
      "org.scalatest" %% "scalatest" % "3.2.19"
    ),
  )
  .dependsOn(dap.jvm)

val mainProgram = Executable("main")
lazy val dapNativeExamples = project.in(file("dap-native-examples"))
  .enablePlugins(CcPlugin)
  .settings(
    name := "dap-native-examples",
    libraryDependencies ++= Seq(
      "org.scalatest" %% "scalatest" % "3.2.19"
    ),
    Compile / ccTargets := ListSet(Executable("main")),
    Compile / cSources := Map(
      mainProgram -> Seq(
        baseDirectory.value / "src" / "main" / "c" / "gossip.c",
      ),
    ),
    Compile / ldFlags := Map(mainProgram -> Seq("-L.", "-ldap")),
  )
  .dependsOn(dap.native)
