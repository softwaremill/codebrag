Codebrag MongoDB Migrations
===========================

Only migrations that were not already applied will be run.

To run database migrations go to mongo_migrations directory in distribution package and run the command below.

/path/to/mongo <HOST:PORT>/<DATABASE> run_migration.js

or if you have Ruby interpreter installed, run

migrate.rb <CODEBRAG_CONFIG_FILE> <PATH_TO_MONGO>

FAQ
===

Q: Getting "JavaScript execution failed: Error: locale::facet::_S_create_c_locale name not valid at ./migrations_engine.js:L54
failed to load: run_migration.js"

A: Your LOCALE setup is broken. Solution is described at http://stackoverflow.com/questions/19100708/mongodb-mongorestore-failure-localefacet-s-create-c-locale-name-not-valid
In short try to export LC_ALL="en_US.UTF-8" and if this doesn't work, generate locale according to your linux distribution specifics.
