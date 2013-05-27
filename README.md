# Codebrag

Run Codebrag with stubbed backend
---

You may want to run Codebrag without backend services e.g. to work on frontend side (HTML, CSS).

In order to do that, go to `codebrag-ui/src/main/webapp` directory and fire up any simple http server that can serve files from current dir e.g. `python -m SimpleHTTPServer`.
This server starts on port 8000 by default.

Then fire `http://localhost:8000/#/commits?nobackend` in your browser. This ***?nobackend*** param is important here - don't miss it! You should see screen with some stubbed commits.

Files under `src/main/webapp/` can be now modified and results should be instantly visible in browser.