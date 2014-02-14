# Codebrag

Developer guide
---

Prerequisites:

1. MongoDB (2.4.6 or newer) - installed from package. Not from Brew, there are problems with tests.
2. Setup `mongo-directory` property. In file `~/.sbt/local.sbt` insert (change path to your local path) `SettingKey[File]("mongo-directory") := file("/path/to/mongo")`
3. OS X 10.8 (mongodb tests are hanging on version 10.7)
4. Sbt version 12.3
5. Add sbt-idea plugin. In file `~/.sbt/plugins/build.sbt` insert line `addSbtPlugin("com.github.mpeltonen" % "sbt-idea" % "1.5.1")`
6. Install nodejs (0.10.13 or newer) and make sure `node` and `npm` are available on `PATH`

Quick Start
---

1. Start mongodb
2. Configure `local.conf` in the `codebrag` home directory basing on `codebrag-rest/src/main/resources/application.conf.template`
3. Navigate to the `codebrag` home directory
4. Execute `./run.sh` script
5. Default browser should open at `localhost:9090`


Working with application:
---
1. Go to project dir
2. Configure `local.conf` in the `codebrag` home directory basing on `codebrag-rest/src/main/resources/application.conf.template`
3. start sbt with `sbt`
4. Generate Intellij Idea project files with `gen-idea` command
5. Open project in Idea
6. run backend server on jetty with `~;container:start; container:reload /`. Project will be recompiled & redeployed every time Scala sources will be changed.
7. Go to `codebrag-ui` project. If this is your first attempt, run `npm install`. This will install all the dependencies required to start UI Codebrag application. Then run `./node_modules/.bin/grunt server`. If you have `grunt` installed globally you can use `grunt server` instead.

Default browser should open at [http://localhost:9090](http://localhost:9090)

For more information about UI application build please consult [README in codebrag-ui project](codebrag-ui/)


Run Codebrag with stubbed backend
---

You may want to run Codebrag without backend services e.g. to work on frontend side (HTML, CSS).
Follow the instructions in `codebrag-ui` project README to install all required stuff.
When Codebrag is run, appending `?nobackend` to any URL lets you work with stubbed data - with no backend required.


Skipping slow tests
---
If you want to execute tests from sbt and skip slow cases requiring database, you can execute following command:
`test-only * -- -l requiresDb`


Logging
---

For logging we use SLF4J+Logback. An example configration file can be found in `scripts/logback-example.xml`. To use a
configuration file, either place a `logback.xml` file in the bundle, or specify an external one using
`-Dlogback.configurationFile`.


Create distribution
---

1. Start sbt, change subproject: `project codebrag-dist`
2. Run: `assembly`
3. This will create a fat-jar. To start Codebrag with the given configuration, run:

````
java -Dconfig.file=[path to .conf file] -Dlogback.configurationFile=logback.xml -jar codebrag-dist-assembly-[version].jar
````

H2 console
---

Web console
===

When using the embedded SQL storage, it may be useful to browse the tables. H2 provides a console, which can be run
as follows:

1. If you have a fat jar, simply run `java -cp codebrag.jar org.h2.tools.Console`, and point the console at your database file
2. From SBT, run: `codebrag-dao/run-h2-console`. This will use your settings file to automatically determine where your database is.

Command line console
===

TBD

Migrating from Mongo to H2
---

1. stop codebrag
2. add the following to your configuration:

````
storage {
    type = "embedded"

    embedded {
        datadir = "/Users/adamw/projects/codebrag/data"
    }
}
````

3. run:

````
java -Dconfig.file=codebrag.conf -cp codebrag.jar com.softwaremill.codebrag.dao.sql.MigrateMongoToSQL
````

4. stop Mongo
5. start Codebrag, and live in a Mongo-free world