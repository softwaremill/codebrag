import java.io.PrintWriter
import sbt._
import Keys._
import com.gu.SbtJasminePlugin._
import net.virtualvoid.sbt.graph.Plugin._
import com.typesafe.sbt.SbtScalariform._
import sbtjslint.Plugin._
import sbtjslint.Plugin.LintKeys._
import sbtassembly.Plugin._
import AssemblyKeys._

object Resolvers {
  val codebragResolvers = Seq(
    "Sonatype releases" at "http://oss.sonatype.org/content/repositories/releases/",
    "Sonatype snapshots" at "http://oss.sonatype.org/content/repositories/snapshots/",
    "SotwareMill Public Releases" at "https://nexus.softwaremill.com/content/repositories/releases/",
    "SotwareMill Public Snapshots" at "https://nexus.softwaremill.com/content/repositories/snapshots/",
    "TorqueBox Releases" at "http://rubygems-proxy.torquebox.org/releases"
  )
}

object BuildSettings {

  import Resolvers._

  val mongoDirectory = SettingKey[File]("mongo-directory", "The home directory of MongoDB datastore")

  val buildSettings = Defaults.defaultSettings ++ Seq(mongoDirectory := file("")) ++ defaultScalariformSettings ++ Seq(

    organization := "pl.softwaremill",
    version := "0.0.1-SNAPSHOT",
    scalaVersion := "2.10.2",

    resolvers := codebragResolvers,
    scalacOptions += "-unchecked",
    classpathTypes ~= (_ + "orbit"),
    libraryDependencies ++= Dependencies.testingDependencies,
    libraryDependencies ++= Dependencies.logging,
    libraryDependencies ++= Seq(Dependencies.guava, Dependencies.googleJsr305),

    concurrentRestrictions in Global += Tags.limit(Tags.Test, 1), // no parallel execution of tests, because we are starting mongo in tests

    testOptions in Test <+= mongoDirectory map {
      md: File => Tests.Setup {
        () =>
          val mongoFile = new File(md.getAbsolutePath + "/bin/mongod")
          val mongoFileWin = new File(mongoFile.getAbsolutePath + ".exe")
          if (mongoFile.exists || mongoFileWin.exists) {
            System.setProperty("mongo.directory", md.getAbsolutePath)
          } else {
            throw new RuntimeException(
              "Trying to launch with MongoDB but unable to find it in 'mongo.directory' (%s). Please check your ~/.sbt/local.sbt file.".format(mongoFile.getAbsolutePath))
          }
      }
    },

    /*
    swagger-core has a dependency to the slf4j -> log4j bridge, while we are using the log4j -> slf4j bridge.
    We cannot exclude the dependency in the dependency declaration, as swagger-core is a transitive dep of
    scalatra-swagger, hence the global exclude.
     */
    ivyXML :=
      <dependencies>
        <exclude org="org.slf4j" artifact="slf4j-log4j12" />
        <exclude org="log4j" artifact="log4j" />
      </dependencies>
  )

}

object Dependencies {

  val slf4jVersion = "1.7.2"
  val logBackVersion = "1.0.9"
  val scalatraVersion = "2.2.1"
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
  val json4s = "org.json4s" %% "json4s-jackson" % "3.1.0"
  val scalatraAuth = "org.scalatra" %% "scalatra-auth" % scalatraVersion  exclude("commons-logging", "commons-logging")

  val swaggerCore = "com.wordnik"  % "swagger-project_2.10.0"  % "1.2.5"
  val scalatraSwagger = "org.scalatra" %% "scalatra-swagger"  % scalatraVersion

  val jodaTime = "joda-time" % "joda-time" % "2.0"
  val jodaConvert = "org.joda" % "joda-convert" % "1.2"

  val commonsValidator = "commons-validator" % "commons-validator" % "1.4.0" exclude("commons-logging", "commons-logging")
  val commonsLang = "org.apache.commons" % "commons-lang3" % "3.1"

  val jetty = "org.eclipse.jetty" % "jetty-webapp" % jettyVersion
  val jettyContainer = "org.eclipse.jetty" % "jetty-webapp" % jettyVersion % "container"

  val mockito = "org.mockito" % "mockito-all" % "1.9.5" % "test"
  val scalatest = "org.scalatest" % "scalatest_2.10" % "1.9.1" % "test"

  val jodaDependencies = Seq(jodaTime, jodaConvert)
  val scalatraStack = Seq(scalatra, scalatraScalatest, scalatraJson, json4s, scalatraAuth, commonsLang, swaggerCore, scalatraSwagger)

  val akka = "com.typesafe.akka" %% "akka-actor" % akkaVersion
  val akkaSlf4j = "com.typesafe.akka" %% "akka-slf4j" % akkaVersion
  val akkaTestkit = "com.typesafe.akka" %% "akka-testkit" % akkaVersion % "test"
  val typesafeConfig = "com.typesafe" % "config" % "1.0.1"

  val testingDependencies = Seq(mockito, scalatest, akkaTestkit)

  val javaxMail = "javax.mail" % "mail" % "1.4.5"

  val scalate = "org.fusesource.scalate" %% "scalate-core" % "1.6.0"

  val seleniumVer = "2.33.0"
  val seleniumJava = "org.seleniumhq.selenium" % "selenium-java" % seleniumVer % "test"
  val seleniumFirefox = "org.seleniumhq.selenium" % "selenium-firefox-driver" % seleniumVer % "test"
  val fest = "org.easytesting" % "fest-assert" % "1.4" % "test"
  val awaitility = "com.jayway.awaitility" % "awaitility-scala" % "1.3.5" % "test"

  val selenium = Seq(seleniumJava, seleniumFirefox, fest)

  // If the scope is provided;test, as in scalatra examples then gen-idea generates the incorrect scope (test).
  // As provided implies test, so is enough here.
  val servletApiProvided = "org.eclipse.jetty.orbit" % "javax.servlet" % "3.0.0.v201112011016" % "provided" artifacts (Artifact("javax.servlet", "jar", "jar"))

  val bson = "com.mongodb" % "bson" % "2.7.1" % "provided"

  val rogueField = "com.foursquare" %% "rogue-field" % rogueVersion intransitive()
  val rogueCore = "com.foursquare" %% "rogue-core" % rogueVersion intransitive()
  val rogueLift = "com.foursquare" %% "rogue-lift" % rogueVersion intransitive()
  val rogueIndex = "com.foursquare" %% "rogue-index" % rogueVersion intransitive()
  val liftMongoRecord = "net.liftweb" %% "lift-mongodb-record" % "2.5.1"
  val rogue = Seq(rogueCore, rogueField, rogueLift, rogueIndex, liftMongoRecord)

  val egitGithubApi = "org.eclipse.mylyn.github" % "org.eclipse.egit.github.core" % "2.1.3"
  val jGit = "org.eclipse.jgit" % "org.eclipse.jgit" % "2.3.1.201302201838-r"
  val dispatch = "net.databinder.dispatch" %% "dispatch-core" % "0.9.5"
}

object SmlCodebragBuild extends Build {

  import Dependencies._
  import BuildSettings._
  import com.github.siasia.WebPlugin.webSettings

  val genCommitSHAFile = TaskKey[Unit](
    "gen-commit-file",
    "Generates a file in target/classes containing SHA of current git commit"
  )

  val gitCommitGenSettings = Seq[Setting[_]](genCommitSHAFile <<= SHAFile)

  lazy val SHAFile = {
    (scalaVersion, baseDirectory, projectID) map { (sv, bd, pid) =>
      val targetProperties: File = bd / "target" / "scala-2.10" / "classes" / "commit.sha"
      replaceFileContent(targetProperties, currentGitCommitSHA)
      println("Generated version file in: " + targetProperties.getPath)
    }
  }

  def currentGitCommitSHA = Process("git rev-parse HEAD")!!

  def replaceFileContent(file: File, content: String) {
    println("Writing commit SHA to " + file)
    if(file.exists) file.delete()
    file.createNewFile()
    val writer = new PrintWriter(file)
    writer print content
    writer.close()
  }

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



  lazy val parent: Project = Project(
    "codebrag-root",
    file("."),
    settings = buildSettings
  ) aggregate(common, domain, dao, service, rest, ui, dist)

  lazy val common: Project = Project(
    "codebrag-common",
    file("codebrag-common"),
    settings = buildSettings ++ Seq(libraryDependencies ++= Seq(bson) ++ jodaDependencies ++ Seq(akka) ++ Seq(akkaSlf4j))
  )

  lazy val domain: Project = Project(
    "codebrag-domain",
    file("codebrag-domain"),
    settings = buildSettings ++ Seq(libraryDependencies ++= Seq(bson))
  ) dependsOn (common)

  lazy val dao: Project = Project(
    "codebrag-dao",
    file("codebrag-dao"),
    settings = buildSettings ++ Seq(libraryDependencies ++= rogue ++ Seq(typesafeConfig))
  ) dependsOn(domain % "test->test;compile->compile", common)

  lazy val service: Project = Project(
    "codebrag-service",
    file("codebrag-service"),
    settings = buildSettings ++ Seq(libraryDependencies ++= Seq(commonsValidator,
      javaxMail, scalate, egitGithubApi, jGit, dispatch, json4s))
  ) dependsOn(domain, common, dao % "test->test;compile->compile")

  lazy val rest: Project = Project(
    "codebrag-rest",
    file("codebrag-rest"),
    settings = buildSettings ++ graphSettings ++ webSettings ++ gitCommitGenSettings ++ Seq(libraryDependencies ++= scalatraStack ++ jodaDependencies ++ Seq(servletApiProvided, typesafeConfig, jettyContainer)) ++ Seq(
      artifactName := { (config: ScalaVersion, module: ModuleID, artifact: Artifact) =>
        "codebrag." + artifact.extension // produces nice war name -> http://stackoverflow.com/questions/8288859/how-do-you-remove-the-scala-version-postfix-from-artifacts-builtpublished-wi
      },
      (copyResources in Compile) <<= (copyResources in Compile) dependsOn (genCommitSHAFile))
  ) dependsOn(service % "test->test;compile->compile", domain, common)


  lazy val dist = Project(
    "codebrag-dist",
    file("codebrag-dist"),
    settings = buildSettings ++ assemblySettings ++ Seq(
      libraryDependencies ++= Seq(jetty),
      mainClass in assembly := Some("com.softwaremill.codebrag.Codebrag"),
      mergeStrategy in assembly <<= (mergeStrategy in assembly) { (old) => {
        // There are two of such files in jgit and javax.servlet - but we don't really care about them (I guess ... ;) )
        // Probably some OSGi stuff.
        case "plugin.properties" => MergeStrategy.discard
        case PathList("META-INF", "eclipse.inf") => MergeStrategy.discard
        // Here we don't care for sure.
        case "about.html" => MergeStrategy.discard
        case x => old(x)
      } },
      // We need to include the whole webapp, hence replacing the resource directory
      unmanagedResourceDirectories in Compile <<= baseDirectory { bd => {
        List(bd.getParentFile() / rest.base.getName / "src" / "main", bd.getParentFile() / ui.base.getName / "dist")
        }
      }
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
