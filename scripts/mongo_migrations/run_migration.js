(function() {
    load('./migrations_engine.js');

    var dbConfig = {
        srcDir: './scripts/',
        serverUrl: 'localhost:27017',
        dbName: 'codebrag'
    };

    var m = new MigrationsEngine(dbConfig);
    m.migrationsToRun();
    m.run();
})();