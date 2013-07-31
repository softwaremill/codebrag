// needs to be global so that scripts can access it
var migrations = [];

var MigrationsEngine = function(config) {

    var migrationsLogCollection = 'migrations_log';
    var db = new Mongo(config.serverUrl).getDB(config.dbName);

    loadAllMigrations();
    discardAlreadyExecutedMigrations();

    this.migrationsToRun = function() {
        if(!migrations.length) {
            print("All migrations were run already");
            return;
        }
        print("-----");
        print("Migrations to execute");
        migrations.forEach(function(migration) {
            print(migration.name);
        });
        print("-----");
    };

    this.run = function() {
        if(!migrations.length) {
            print("Looks like all is up to date. Exiting");
            return;
        }
        print("Running migrations");
        migrations.forEach(function(migration) {
            print(migration.name);
            migration.action(db);
            logMigrationExecuted(migration.name);
            print("-----");
        });
        print("Done");
    };

    function logMigrationExecuted(name) {
        db[migrationsLogCollection].insert({migrationName: name, date: new Date()});
    }

    function discardAlreadyExecutedMigrations() {
        var alreadyExecuted = db[migrationsLogCollection].find().map(function(record) {
            return record.migrationName;
        });
        migrations = migrations.filter(function(migration) {
            return alreadyExecuted.filter(function(executed) {
                return executed === migration.name;
            }).length == 0;
        });
    }

    function loadAllMigrations() {
        listFiles(config.srcDir).map(function(migrationFile) {
            return migrationFile.name;
        }).sort().forEach(function(migrationFile) {
                load(migrationFile);
            });
    }

};