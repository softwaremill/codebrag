# Codebrag

Below is a developer guide if you want to hack on Codebrag yourself.
If you are just looking for installation or upgrade instructions,
please refer to the [wiki](https://github.com/softwaremill/codebrag/wiki)

[Changelog](CHANGELOG.md)

Developer guide
---

Prerequisites:

1. sbt 0.13.6
2. nodejs 0.10.13 or newer (make sure `node` and `npm` are available on `PATH`)

Quick Start
---

1. Configure `local.conf` in the `codebrag` home directory basing on `codebrag-rest/src/main/resources/application.conf.template`
2. Checkout a SVN/Git repository to chosen `repos-root` folder - Codebrag won't do that for you!
3. Navigate to the `codebrag` home directory
4. Execute `./run.sh` script
5. Default browser should open at `localhost:9090`

Working with the application:
---
1. Go to project dir
2. Configure `local.conf` in the `codebrag` home directory basing on `codebrag-rest/src/main/resources/application.conf.template`
3. Start sbt with `sbt`
4. Open project in your favorite IDE
5. Run backend server on jetty with `~ container:start`. Project will be recompiled & redeployed every time Scala sources will be changed.
6. Go to `codebrag-ui` project. If this is your first attempt, run `npm install`. This will install all the dependencies required to start UI Codebrag application. Then run `./node_modules/.bin/grunt server`. If you have `grunt` installed globally you can use `grunt server` instead.

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

When using the embedded SQL storage, it may be useful to browse the tables. H2 provides consoles, which can be run
as follows:

1. For a web console, run from sbt: `codebrag-dao/run-h2-console`
2. For a command line console, run `java -Dconfig.file=codebrag.conf -cp [path to the fat JAR] com.softwaremill.codebrag.dao.sql.H2ShellConsole`
