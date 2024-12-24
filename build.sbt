import scala.scalanative.build.{BuildTarget, GC, LTO, Mode}

ThisBuild / scalaVersion := "3.4.2"
ThisBuild / logLevel := Level.Debug

lazy val nativeCommonSettings = Seq(
    nativeConfig ~= {
        _.withGC(GC.default) // garbage collector
          .withLTO(LTO.none) // link-time optimization
          .withMode(Mode.debug) // build mode
          .withLinkingOptions(Seq()) // a sequence of additional linker options to be passed to the native linker
          .withBuildTarget(BuildTarget.libraryDynamic) // build target: dynamic library, static library, executable
    },
)

lazy val jsCommonSettings = Seq(
    scalaJSUseMainModuleInitializer := true,
)

lazy val dap = crossProject(JVMPlatform, NativePlatform)
  .crossType(CrossType.Full)
  .in(file("dap"))
  .settings(
    name := "dap",
    libraryDependencies ++= Seq(
      "org.scalatest" %%% "scalatest" % "3.2.19"
    )
  )
  .nativeSettings(nativeCommonSettings *)

lazy val experiments = crossProject(JVMPlatform, NativePlatform)
  .crossType(CrossType.Full)
  .in(file("experiments"))
  .settings(
    name := "experiments",
    libraryDependencies ++= Seq(
      "org.scalatest" %%% "scalatest" % "3.2.19"
    )
  )
  .nativeSettings(nativeCommonSettings *)
