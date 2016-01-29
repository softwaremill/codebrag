import java.text.SimpleDateFormat
import java.util.Date

import com.typesafe.sbt.SbtScalariform._
import net.virtualvoid.sbt.graph.Plugin._
import sbt.Keys._
import sbt._
import sbtassembly.Plugin.AssemblyKeys._
import sbtassembly.Plugin._
import sbtbuildinfo.Plugin._

object Resolvers {
  val codebragResolvers = Seq(
    "Sonatype releases" at "http://oss.sonatype.org/content/repositories/releases/",
    "Sonatype snapshots" at "http://oss.sonatype.org/content/repositories/snapshots/",
    "SotwareMill Public Releases" at "https://nexus.softwaremill.com/content/repositories/releases/",
    "SotwareMill Public Snapshots" at "https://nexus.softwaremill.com/content/repositories/snapshots/",
    "TorqueBox Releases" at "http://rubygems-proxy.torquebox.org/releases",
    "RoundEights" at "http://maven.spikemark.net/roundeights"
  )
}
object BuildSettings {

  import Resolvers._

  val buildSettings = Defaults.coreDefaultSettings ++ net.virtualvoid.sbt.graph.Plugin.graphSettings ++
    defaultScalariformSettings ++ Seq(

    organization := "com.softwaremill",
    version := "2.3.2",
    scalaVersion := "2.10.4",

    resolvers := codebragResolvers,
    scalacOptions += "-unchecked",
    classpathTypes ~= (_ + "orbit"),
    libraryDependencies ++= Dependencies.testingDependencies,
    libraryDependencies ++= Dependencies.logging,
    libraryDependencies ++= Seq(Dependencies.guava, Dependencies.googleJsr305),

    concurrentRestrictions in Global += Tags.limit(Tags.Test, 1) // no parallel execution of tests, because we are starting mongo in tests
  )
}

object Dependencies {

  val slf4jVersion = "1.7.2"
  val logBackVersion = "1.0.9"
  val scalatraVersion = "2.2.2"
  val rogueVersion = "2.1.0"
  val scalaLoggingVersion = "1.0.1"
  val akkaVersion = "2.1.4"
  val jettyVersion = "8.1.7.v20120910"

  val slf4jApi = "org.slf4j" % "slf4j-api" % slf4jVersion
  val logBackClassic = "ch.qos.logback" % "logback-classic" % logBackVersion
  val log4jOverSlf4j = "org.slf4j" % "log4j-over-slf4j" % slf4jVersion
  val scalaLogging = "com.typesafe" %% "scalalogging-slf4j" % scalaLoggingVersion

  val logging = Seq(slf4jApi, logBackClassic, scalaLogging, log4jOverSlf4j)

  val guava = "com.google.guava" % "guava" % "13.0.1"
  val googleJsr305 = "com.google.code.findbugs" % "jsr305" % "2.0.1"

  val scalatra = "org.scalatra" %% "scalatra" % scalatraVersion
  val scalatraScalatest = "org.scalatra" %% "scalatra-scalatest" % scalatraVersion % "test"
  val scalatraJson = "org.scalatra" %% "scalatra-json" % scalatraVersion
  val json4s = "org.json4s" %% "json4s-jackson" % "3.2.10"
  val json4sExt = "org.json4s" %% "json4s-ext" % "3.2.10"
  val scalatraAuth = "org.scalatra" %% "scalatra-auth" % scalatraVersion  exclude("commons-logging", "commons-logging")

  val jodaTime = "joda-time" % "joda-time" % "2.0"
  val jodaConvert = "org.joda" % "joda-convert" % "1.2"

  val commonsValidator = "commons-validator" % "commons-validator" % "1.4.0" exclude("commons-logging", "commons-logging")
  val commonsLang = "org.apache.commons" % "commons-lang3" % "3.1"
  val commonsCodec = "commons-codec" % "commons-codec" % "1.8"

  val jetty = "org.eclipse.jetty" % "jetty-webapp" % jettyVersion

  val mockito = "org.mockito" % "mockito-all" % "1.9.5" % "test"
  val scalatest = "org.scalatest" % "scalatest_2.10" % "1.9.1" % "test"

  val jodaDependencies = Seq(jodaTime, jodaConvert)
  val scalatraStack = Seq(scalatra, scalatraScalatest, scalatraJson, json4s, scalatraAuth, commonsLang)

  val akka = "com.typesafe.akka" %% "akka-actor" % akkaVersion
  val akkaSlf4j = "com.typesafe.akka" %% "akka-slf4j" % akkaVersion
  val akkaTestkit = "com.typesafe.akka" %% "akka-testkit" % akkaVersion % "test"
  val typesafeConfig = "com.typesafe" % "config" % "1.0.1"

  val testingDependencies = Seq(mockito, scalatest, akkaTestkit)

  val javaxMail = "javax.mail" % "mail" % "1.4.5"

  val scalate = "org.fusesource.scalate" %% "scalate-core" % "1.6.1"

  val seleniumVer = "2.33.0"
  val seleniumJava = "org.seleniumhq.selenium" % "selenium-java" % seleniumVer % "test"
  val seleniumFirefox = "org.seleniumhq.selenium" % "selenium-firefox-driver" % seleniumVer % "test"
  val fest = "org.easytesting" % "fest-assert" % "1.4" % "test"
  val awaitility = "com.jayway.awaitility" % "awaitility-scala" % "1.3.5" % "test"

  val selenium = Seq(seleniumJava, seleniumFirefox, fest)

  // If the scope is provided;test, as in scalatra examples then gen-idea generates the incorrect scope (test).
  // As provided implies test, so is enough here.
  val servletApiProvided = "org.eclipse.jetty.orbit" % "javax.servlet" % "3.0.0.v201112011016" % "provided" artifacts (Artifact("javax.servlet", "jar", "jar"))

  val bson = "org.mongodb" % "bson" % "2.7.1"

  val egitGithubApi = "org.eclipse.mylyn.github" % "org.eclipse.egit.github.core" % "2.1.3"
  val jGit = "org.eclipse.jgit" % "org.eclipse.jgit" % "2.3.1.201302201838-r"
  val jsch = "com.jcraft" % "jsch" % "0.1.51"
  val dispatch = "net.databinder.dispatch" %% "dispatch-core" % "0.9.5"

  val slick = "com.typesafe.slick" %% "slick" % "2.0.3"
  val h2 = "com.h2database" % "h2" % "1.3.175"
  val flyway = "com.googlecode.flyway" % "flyway-core" % "2.3"
  val c3p0 = "com.mchange" % "c3p0" % "0.9.5-pre6"

  val scalaval = "com.softwaremill.scalaval" %% "scalaval" % "0.1"

  val httpClient = "net.databinder.dispatch" %% "dispatch-core" % "0.11.2"

  val crypto = "com.roundeights" %% "hasher" % "1.0.0"

}

object SmlCodebragBuild extends Build {

  import BuildSettings._
  import Dependencies._
  import com.earldouglas.xwp._

  val buildWebClient = TaskKey[Unit](
    "build-web-client",
    "Builds browser client using Grunt.js"
  )

  val webClientBuildSettings = Seq[Setting[_]](buildWebClient <<= {
    (scalaVersion, baseDirectory, projectID) map {
      (sv, bd, pid) => {
        val localGruntCommand = "./node_modules/.bin/grunt build"
        def updateDeps(cwd: File) = Process("npm install", cwd)!
        def runGrunt(cwd: File) = Process(localGruntCommand, cwd)!
        def haltOnError(result: Int) {
          if(result != 0) {
            throw new Exception("Building web client failed")
          }
        }
        println("Updating NPM dependencies")
        haltOnError(updateDeps(bd))
        println("Building with Grunt.js")
        haltOnError(runGrunt(bd))

      }
    }
  })

  val runH2Console = TaskKey[Unit]("run-h2-console", "Runs the H2 console using the data file from the local config file")
  val runH2ConsoleSettings = fullRunTask(runH2Console, Compile, "com.softwaremill.codebrag.dao.sql.H2BrowserConsole")

  lazy val parent: Project = Project(
    "codebrag-root",
    file("."),
    settings = buildSettings
  ) aggregate(common, domain, dao, service, rest, ui, dist)

  lazy val common: Project = Project(
    "codebrag-common",
    file("codebrag-common"),
    settings = buildSettings ++ Seq(libraryDependencies ++= Seq(bson) ++ jodaDependencies ++ Seq(commonsCodec, typesafeConfig, crypto)) ++ buildInfoSettings ++
      Seq(
        sourceGenerators in Compile <+= buildInfo,
        buildInfoPackage := "com.softwaremill.codebrag.version",
        buildInfoObject := "CodebragBuildInfo",
        buildInfoKeys := Seq[BuildInfoKey](
          version,
          BuildInfoKey.action("buildDate")(new SimpleDateFormat("yyyy-MM-dd HH:mm").format(new Date())),
          BuildInfoKey.action("buildSha")((Process("git rev-parse HEAD") !!).stripLineEnd)
        )
      )
  )

  lazy val domain: Project = Project(
    "codebrag-domain",
    file("codebrag-domain"),
    settings = buildSettings ++ Seq(libraryDependencies ++= Seq(bson, json4s, json4sExt, commonsLang) ++ jodaDependencies) ++ assemblySettings ++ Seq(
      assemblyOption in assembly ~= { _.copy(includeScala = false) },
      excludedJars in assembly <<= (fullClasspath in assembly) map { cp =>
        cp filter {_.data.getName == "config-1.0.1.jar"}
      }
    )
  ) dependsOn (common)

  lazy val dao: Project = Project(
    "codebrag-dao",
    file("codebrag-dao"),
    settings = buildSettings ++ Seq(libraryDependencies ++= Seq(bson, typesafeConfig, slick, h2, flyway, c3p0), runH2ConsoleSettings)
  ) dependsOn(domain % "test->test;compile->compile", common)

  lazy val service: Project = Project(
    "codebrag-service",
    file("codebrag-service"),
    settings = buildSettings ++ Seq(libraryDependencies ++= Seq(commonsValidator,
      javaxMail, scalate, egitGithubApi, jGit, jsch, dispatch, json4s, json4sExt, commonsLang, scalaval, akka, akkaSlf4j))
  ) dependsOn(domain, common, dao % "test->test;compile->compile")

  lazy val rest: Project = Project(
    "codebrag-rest",
    file("codebrag-rest"),
    settings = buildSettings ++ 
      graphSettings ++ 
      XwpPlugin.jetty() ++ 
      Seq(libraryDependencies ++= scalatraStack ++ jodaDependencies ++ Seq(servletApiProvided, typesafeConfig)) ++
      Seq(
        artifactName := { (config: ScalaVersion, module: ModuleID, artifact: Artifact) =>
          "codebrag." + artifact.extension // produces nice war name -> http://stackoverflow.com/questions/8288859/how-do-you-remove-the-scala-version-postfix-from-artifacts-builtpublished-wi
        }
      ) ++
      Seq(javaOptions in XwpPlugin.container := Seq("-Dconfig.file=local.conf"))
  ) dependsOn(service % "test->test;compile->compile", domain, common)


  lazy val dist = Project(
    "codebrag-dist",
    file("codebrag-dist"),
    settings = buildSettings ++ assemblySettings ++ Seq(
      libraryDependencies ++= Seq(jetty),
      mainClass in assembly := Some("com.softwaremill.codebrag.Codebrag"),
      // We need to include the whole webapp, hence replacing the resource directory
      unmanagedResourceDirectories in Compile <<= baseDirectory { bd =>
        List(bd.getParentFile() / rest.base.getName / "src" / "main", bd.getParentFile() / ui.base.getName / "dist")
      },
      mergeStrategy in assembly <<= (mergeStrategy in assembly) { (old) => {
        // There are two of such files in jgit and javax.servlet - but we don't really care about them (I guess ... ;) 
        // Probably some OSGi stuff.
        case "plugin.properties" => MergeStrategy.discard
        case PathList("META-INF", "eclipse.inf") => MergeStrategy.discard
        // Here we don't care for sure.
        case "about.html" => MergeStrategy.discard
        case x => old(x)
      } }
    )
  ) dependsOn (ui, rest)

  lazy val ui = Project(
    "codebrag-ui",
    file("codebrag-ui"),
    settings = buildSettings ++ webClientBuildSettings ++ Seq(
      (compile in Compile) <<= (compile in Compile) dependsOn (buildWebClient)
    )
  )

  lazy val uiTests = Project(
    "codebrag-ui-tests",
    file("codebrag-ui-tests"),
    settings = buildSettings ++ Seq(
      libraryDependencies ++= selenium ++ Seq(awaitility)
    )

  ) dependsOn (dist)

  // To run the embedded container, we need to provide the path to the configuration. To make things easier, we assume
  // that the local conf is in the current dir in the local.conf file.
  System.setProperty("config.file", "local.conf")

}
