# Codebrag MongoDB Migrations

Scripts
--

Each script is written in JS and can be executed by mongo shell.
Script names start with current timestamp followed by snake_case short description of changes.
Each script should be in form of:

    migrations.push({name: 'MIGRATION DESCRIPTION', action: function(db) {
        your operations on already opened db
    }});

When executed each script needs to push object with `name` and `action` properties to `migrations` collection.


Execution
--

Only migrations that were not executed already are run. There is `migrations_log` collection in database that keeps track of already executed migrations.
Each migration name together with run-date is stored in this collection upon successful run. On the next run this collection is consulted and only newer migrations are run.

Run
--

Just run migrations with `/path/to/mongo <HOST:PORT>/<DATABASE> run_migration.js`.
