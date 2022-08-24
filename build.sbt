ThisBuild / tlBaseVersion := "0.0"

ThisBuild / developers := List(
  tlGitHubDev("armanbilge", "Arman Bilge")
)
ThisBuild / startYear := Some(2022)

ThisBuild / crossScalaVersions := Seq("3.1.3")
ThisBuild / githubWorkflowJavaVersions := Seq(JavaSpec.temurin("17"))
ThisBuild / githubWorkflowOSes += "macos-latest"
ThisBuild / tlJdkRelease := Some(8)

ThisBuild / githubWorkflowEnv ++=
  Map(
    "CPPFLAGS" -> "-I/opt/homebrew/opt/curl/include",
    "LDFLAGS" -> "-L/opt/homebrew/opt/curl/lib",
  )
ThisBuild / githubWorkflowBuildPreamble ++= Seq(
  WorkflowStep.Run(
    List("sudo apt-get update", "sudo apt-get install libcurl4-openssl-dev"),
    name = Some("Install libcurl"),
    cond = Some("matrix.os == 'ubuntu-latest'"),
  ),
  WorkflowStep.Run(
//    List("brew list curl", "brew install --force curl", "brew list curl"),
    List("brew link curl"),
    name = Some("Link libcurl - macOS"),
    cond = Some("matrix.os == 'macos-latest'"),
  ),
)
ThisBuild / githubWorkflowBuildPostamble ~= {
  _.filterNot(_.name.contains("Check unused compile dependencies"))
}

val http4sVersion = "0.23.14-101-02562a0-SNAPSHOT"
val munitCEVersion = "2.0-4e051ab-SNAPSHOT"
ThisBuild / resolvers ++= Resolver.sonatypeOssRepos("snapshots")

lazy val root = project.in(file(".")).enablePlugins(NoPublishPlugin).aggregate(curl, example)

lazy val curl = project
  .in(file("curl"))
  .enablePlugins(ScalaNativePlugin)
  .settings(
    name := "http4s-curl",
    libraryDependencies ++= Seq(
      "com.armanbilge" %%% "http4s-client" % http4sVersion,
      "com.armanbilge" %%% "munit-cats-effect" % munitCEVersion % Test,
    ),
  )

lazy val example = project
  .in(file("example"))
  .enablePlugins(ScalaNativePlugin, NoPublishPlugin)
  .dependsOn(curl)
  .settings(
    libraryDependencies ++= Seq(
      "com.armanbilge" %%% "http4s-circe" % http4sVersion
    )
  )
