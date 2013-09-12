# Codebrag UI application

Overview
---
Codebrag frontend application project `codebrag-ui` is completely decoupled from server exposing JSON API. It uses `nodejs` based build tool called [Grunt.js](http://gruntjs.com) which is well suited for frontend application development. Main project exposing server API is located in `codebrag-rest`.


Prerequisities
---
To work with `codebrag-ui` you need to have `node` installed in version 0.10.13 or newer. Make sure you have both `node` and `npm` commands available on `PATH`.


First run
---

If this is your first attempt to run `codebrag-ui`, please go to `codebrag-ui` project and run

	npm install
	
This will install all required dependencies for this project.

Installing Grunt.js
---

#### Global installation

It is adised to install Grunt.js globally. In order to do that, please run 
	
	npm install -g grunt-cli

This will install grunt command globally and make it available. Be sure to have NPM binaries on `PATH`. You may need to add '/usr/local/share/npm/bin/' to your PATH if you have NPM installed via Homebrew.
Then you can use `grunt` command as described below.

#### Local installation

If you don't want to install Grunt.js locally, Codebrag has it already in dependencies, so doing `npm install` as described above should be enough. The only drawback is that in this mode `grunt` will not be available on your `PATH`. To run grunt from local installation you should be in `codebrag-ui` project and run it via

	./node_modules/.bin/grunt
	

Development
---
Build system exposes several tasks that can be run. `Gruntfile.js` contains all the build configuration. Run it with `grunt <task>` if you have grunt installed globally or via `./node_modules/.bin/grunt <task>` if installed locally.

The most important tasks exposed are:

- `grunt server`
- `grunt server:dist`
- `grunt build`
- `grunt test`
- `grunt autotest`

`Grunt server` task
---
This task serves Codebrag application on port `9090` on `localhost`. Your default browser should open at this location. All requests to API for data will be proxied to port `8080` when it expects backend server to be run.

Grunt will watch for any change in frontend files (templates, js files, styles) and every change is automatically compiled (if necessary) and browser is automatically refreshed to apply changes. No need to refresh it by hand. 

*** Note: if you have LiveReload extension enabled in you browser, please disable it so that it doesn't interfere with build one ***

In this task all scripts are serverd in non-concatenated and non-minified version from their original locations (if possible).

`Grunt server:dist` task
---
This task is similar to the one above with one difference: it preprocessess all the files in order to create distribution (it currently includes concatenation of scripts files), runs tests and serves application from this freshly baked distribution version. This server's version doesn't watch for file changes.

`Grunt build` task
---
It runs all tests and builds everything to as distribution version to `dist` directory. It doesn't fire up server.

`Grunt test` task
---
It simply tests the build one time. Tests are run with Karma runner using PhantomJS as default browser. Whole tests configuration is in `karma-config.js` file in `codebrag-ui` project.

`Grunt autotest` task
---
This task runs tests and watches for changes in files. When change is detected it runs tests automatically. This is especially helpful in hard-development mode.

Distribution and deployment
---
Nothing changed in distribution and deployment procedure. During fat-jar creation. Grunt task `grunt build` is run and distribution version of frontend is packaged up in this jar as if it was regular java web application.