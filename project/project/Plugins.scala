import sbt._
import Keys._

object Plugins extends Build {
  lazy val plugins = Project(
    "plugins",
    file("."),
    settings = Defaults.defaultSettings ++ Seq(
      addSbtPlugin("com.github.mpeltonen" % "sbt-idea" % "1.4.0"),
      addSbtPlugin("net.virtual-void" % "sbt-dependency-graph" % "0.7.0"),
      addSbtPlugin("com.typesafe.sbt" % "sbt-scalariform" % "1.0.1"),
      addSbtPlugin("com.github.philcali" % "sbt-jslint" % "0.1.3"),
      addSbtPlugin("com.eed3si9n" % "sbt-assembly" % "0.9.0")))
      .dependsOn(
    // TeamCity reporting, see: https://github.com/guardian/sbt-teamcity-test-reporting-plugin
    uri("git://github.com/guardian/sbt-teamcity-test-reporting-plugin.git#1.2")
  )

}
