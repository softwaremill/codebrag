(function() {
    load('./migrations_engine.js');

    var m = new MigrationsEngine();
    m.migrationsToRun();
    m.run();
})();