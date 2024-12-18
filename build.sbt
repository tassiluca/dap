import scala.scalanative.build.{BuildTarget, GC, LTO, Mode}

ThisBuild / scalaVersion := "3.4.2"
ThisBuild / logLevel := Level.Debug

lazy val proof = crossProject(JSPlatform, JVMPlatform, NativePlatform)
  .crossType(CrossType.Full)
  .in(file("."))
  .settings(
    name := "proof-of-concept",
    libraryDependencies ++= Seq(
      "org.scalatest" %%% "scalatest" % "3.2.19"
    )
  )
  .jsSettings(
    scalaJSUseMainModuleInitializer := true
  )
  .nativeSettings(
    nativeConfig ~= {
      _.withLinkingOptions(Seq("-shared"))
        .withLTO(LTO.none)
        .withMode(Mode.debug)
        .withBuildTarget(BuildTarget.libraryDynamic)
    },
  )

lazy val root = (project in file(".")) aggregate (proof.js, proof.jvm, proof.native)
