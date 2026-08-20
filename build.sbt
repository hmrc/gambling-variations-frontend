import play.sbt.routes.RoutesKeys
import sbt.Def
import scoverage.ScoverageKeys
import uk.gov.hmrc.versioning.SbtGitVersioning.autoImport.majorVersion

lazy val appName: String = "gambling-variations-frontend"

lazy val format = taskKey[Unit]("Rewrite Scala sources and conf/messages files into their canonical format")

ThisBuild / majorVersion := 0
ThisBuild / scalaVersion := "3.3.5"

lazy val microservice = (project in file("."))
  .enablePlugins(PlayScala, SbtDistributablesPlugin, msgman.sbtplugin.MsgmanPlugin)
  .disablePlugins(JUnitXmlReportPlugin) // Required to prevent https://github.com/scalatest/scalatest/issues/1427
  .settings(inConfig(Test)(testSettings): _*)
  .settings(ThisBuild / useSuperShell := false)
  .settings(formatCheckSettings: _*)
  .settings(
    name := appName,
    RoutesKeys.routesImport ++= Seq(
      "models._",
      "uk.gov.hmrc.play.bootstrap.binders.RedirectUrl"
    ),
    TwirlKeys.templateImports ++= Seq(
      "play.twirl.api.HtmlFormat",
      "play.twirl.api.HtmlFormat._",
      "uk.gov.hmrc.govukfrontend.views.html.components._",
      "uk.gov.hmrc.hmrcfrontend.views.html.components._",
      "uk.gov.hmrc.hmrcfrontend.views.html.helpers._",
      "uk.gov.hmrc.hmrcfrontend.views.config._",
      "views.ViewUtils._",
      "models.Mode",
      "controllers.routes._",
      "viewmodels.govuk.all._"
    ),
    PlayKeys.playDefaultPort := 10402,
    ScoverageKeys.coverageExcludedFiles := "<empty>;Reverse.*;.*handlers.*;.*components.*;" +
      ".*Routes.*;.*viewmodels.govuk.*;",
    ScoverageKeys.coverageMinimumStmtTotal := 78,
    ScoverageKeys.coverageFailOnMinimum := true,
    ScoverageKeys.coverageHighlighting := true,
    // build.sbt and project/*.scala belong to the build as a whole, so they are only checked here.
    Compile / compile := (Compile / compile).dependsOn(Compile / scalafmtSbtCheck).value,
    // --fix adds a placeholder for any missing translation, so a formatted tree is one that
    // msgmanVerify accepts, and compiling is not blocked by a translation nobody has written yet.
    msgmanFix := true,
    // msgman itself only prints when it has something to report, so a clean run is otherwise
    // silent; the completion lines below make sure it's never unclear whether it actually ran.
    Compile / compile := (Compile / compile)
      .dependsOn(
        Def.sequential(msgmanVerify, Def.task(streams.value.log.info("msgman: verify complete")))
      )
      .value,
    format := Def
      .sequential(
        scalafmtAll,
        Compile / scalafmtSbt,
        LocalProject("it") / scalafmtAll,
        msgmanFormat,
        Def.task(streams.value.log.info("msgman: format complete"))
      )
      .value,
    scalacOptions ++= Seq(
      "-feature",
      "-deprecation",
      "-Wconf:src=html/.*:s",
      "-Wconf:src=routes/.*:s",
      "-Wconf:msg=Flag.*repeatedly:s"
    ),
    libraryDependencies ++= AppDependencies(),
    retrieveManaged := true,
    pipelineStages := Seq(digest),
    Assets / pipelineStages := Seq(concat)
  )

// Compiling fails when sources are not formatted, instead of rewriting them behind your back.
// Run `sbt format` to fix them, along with the conf/messages files.
lazy val formatCheckSettings: Seq[Def.Setting[?]] = Seq(
  Compile / compile := (Compile / compile).dependsOn(Compile / scalafmtCheck).value,
  Test / compile := (Test / compile).dependsOn(Test / scalafmtCheck).value
)

lazy val testSettings: Seq[Def.Setting[?]] = Seq(
  fork := true,
  unmanagedSourceDirectories += baseDirectory.value / "test-utils",
  testOptions += Tests.Argument(TestFrameworks.ScalaTest, "-S", "12345")
)

lazy val it =
  (project in file("it"))
    .enablePlugins(PlayScala)
    .dependsOn(microservice % "test->test")
    .settings(formatCheckSettings: _*)
