import sbt._

object Plugins extends Build {
  lazy val plugins = Project(
    "plugins",
    file("."),
    settings = Defaults.coreDefaultSettings ++ Seq(
      addSbtPlugin("com.earldouglas" %% "xsbt-web-plugin" % "1.0.0-M7"),
addSbtPlugin("com.github.mpeltonen" % "sbt-idea" % "1.6.0"),      
addSbtPlugin("net.virtual-void" % "sbt-dependency-graph" % "0.7.4"),
      addSbtPlugin("com.typesafe.sbt" % "sbt-scalariform" % "1.1.0"),
      addSbtPlugin("com.gu" % "sbt-teamcity-test-reporting-plugin" % "1.5"),
      addSbtPlugin("com.eed3si9n" % "sbt-assembly" % "0.9.0"),
      addSbtPlugin("com.eed3si9n" % "sbt-buildinfo" % "0.2.5"))
  )
}
