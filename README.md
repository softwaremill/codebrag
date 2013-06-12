# Codebrag

Quick Start
---
1. Start mongodb
2. Configure application.conf
If you want to access GitHub repositories, create a file called `application.conf` in your classpath. You can use `application.conf.template`.
Important elements to fill:
    * `syncUserLogin`: login of Codebrag user who will be used for synchronization with GitHub
    * `GitHubClientId` and `GitHubClientSecret`: keys copied from GitHub settings of your Codebrag instance.
    * `localGitPath` (Optional): path to directory where local git repositories should be stored. Useful for developers to avoid having these repositories in same directory as source files.
3. Navigate to the `codebrag` home directory
4. Execute `./run.sh` script
5. Navigate to `localhost:8080` and use it (enter any user/pass to log in, an account will be created automatically)

Run Codebrag with stubbed backend
---

You may want to run Codebrag without backend services e.g. to work on frontend side (HTML, CSS).

In order to do that, go to `codebrag-ui/src/main/webapp` directory and fire up any simple http server that can serve files from current dir e.g. `python -m SimpleHTTPServer`.
This server starts on port 8000 by default.

Then fire `http://localhost:8000/#/commits?nobackend` in your browser. This ***?nobackend*** param is important here - don't miss it! You should see screen with some stubbed commits.

Files under `src/main/webapp/` can be now modified and results should be instantly visible in browser.

Skipping slow tests
---
If you want to execute tests from sbt and skip slow cases requiring database, you can execute following command:
`test-only * -- -l requiresDb`
