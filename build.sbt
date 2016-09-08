name := "featherbed"
import com.typesafe.sbt.SbtGhPages.GhPagesKeys._
import sbtunidoc.Plugin.UnidocKeys._

lazy val buildSettings = Seq(
  organization := "io.github.finagle",
  version := "0.2.1-SNAPSHOT",
  scalaVersion := "2.11.8"
)

val finagleVersion = "6.35.0"
val shapelessVersion = "2.3.0"
val catsVersion = "0.7.2"

lazy val docSettings = Seq(
  autoAPIMappings := true
)

lazy val baseSettings = docSettings ++ Seq(
  libraryDependencies ++= Seq(
    "com.twitter" %% "finagle-http" % finagleVersion,
    "com.chuusai" %% "shapeless" % shapelessVersion,
    "org.typelevel" %% "cats" % catsVersion,
    "org.scalamock" %% "scalamock-scalatest-support" % "3.2.2" % "test",
    "org.scalatest" %% "scalatest" % "2.2.6" % "test"
  ),
  resolvers += Resolver.sonatypeRepo("snapshots")
)

lazy val publishSettings = Seq(
  publishMavenStyle := true,
  publishArtifact := true,
  publishTo := {
    val nexus = "https://oss.sonatype.org/"
    if (isSnapshot.value)
      Some("snapshots" at nexus + "content/repositories/snapshots")
    else
      Some("releases"  at nexus + "service/local/staging/deploy/maven2")
  },
  publishArtifact in Test := false,
  licenses := Seq("Apache 2.0" -> url("http://www.apache.org/licenses/LICENSE-2.0")),
  homepage := Some(url("https://finagle.github.io/featherbed/")),
  autoAPIMappings := true,
  scmInfo := Some(
    ScmInfo(
      url("https://github.com/finagle/featherbed"),
      "scm:git:git@github.com:finagle/featherbed.git"
    )
  ),
  pomExtra :=
    <developers>
      <developer>
        <id>jeremyrsmith</id>
        <name>Jeremy Smith</name>
        <url>https://github.com/jeremyrsmith</url>
      </developer>
    </developers>
)

lazy val noPublish = Seq(
  publish := {},
  publishLocal := {},
  publishArtifact := false
)

lazy val allSettings = publishSettings ++ baseSettings ++ buildSettings

lazy val `featherbed-core` = project
  .settings(allSettings)

lazy val `featherbed-circe` = project
  .settings(allSettings)
  .dependsOn(`featherbed-core`)

val scaladocVersionPath = settingKey[String]("Path to this version's ScalaDoc")
val scaladocLatestPath = settingKey[String]("Path to latest ScalaDoc")
val tutPath = settingKey[String]("Path to tutorials")

lazy val `docs` = project
    .settings(
      allSettings ++ tutSettings ++ ghpages.settings ++ Seq(
        scaladocVersionPath := ("api/" + version.value),
        scaladocLatestPath := (if (isSnapshot.value) "api/latest-snapshot" else "api/latest"),
        tutPath := "doc",
        includeFilter in makeSite := (includeFilter in makeSite).value || "*.md" || "*.yml",
        addMappingsToSiteDir(tut, tutPath),
        addMappingsToSiteDir(mappings in (featherbed, ScalaUnidoc, packageDoc), scaladocLatestPath),
        addMappingsToSiteDir(mappings in (featherbed, ScalaUnidoc, packageDoc), scaladocVersionPath),
        ghpagesNoJekyll := false,
        git.remoteRepo := "git@github.com:finagle/featherbed"
      )
    ).dependsOn(`featherbed-core`, `featherbed-circe`)


lazy val featherbed = project
  .in(file("."))
  .settings(unidocSettings ++ baseSettings ++ buildSettings)
  .aggregate(`featherbed-core`, `featherbed-circe`)
