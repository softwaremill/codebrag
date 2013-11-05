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

Follow the instructions in `codebrag-ui-grunt` project README to install all required stuff.

Files under `src/main/webapp/` can be now modified and results should be instantly visible in browser.


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
