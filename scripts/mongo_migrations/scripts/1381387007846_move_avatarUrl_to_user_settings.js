/**
 * Moves field 'avatarUrl' from root document to a subdocument with user settings.
 */
migrations.push({name: 'Move avatarUrl to user settings', action: function (db) {

    var existingRecordsQuery = {avatarUrl: {$exists: true}};

    migrateCollectionUsers();

    function migrateCollectionUsers() {
        var existingRecords = db['users'].find(existingRecordsQuery);
        if (!existingRecords.count()) {
            print('No records in collection "users" to migrate - skipping');
            return;
        }

        print('Migrating ' + existingRecords.count() + ' records in collection "users"');
        var newRecords = buildNewRecords(existingRecords)
        removeOldRecords();
        insertMigratedRecords(newRecords);
        print('Done')
    }

    function buildNewRecords(records) {
        return records.map(function (record) {
            record.userSettings = {};
            record.userSettings.avatarUrl = record.avatarUrl;
            record.userSettings.emailNotificationsEnabled = true;
            delete record.avatarUrl;
            return record;
        });
    }

    function insertMigratedRecords(newRecords) {
        db['users'].insert(newRecords);
        exitIfError();
    }

    function removeOldRecords() {
        db['users'].remove(existingRecordsQuery);
        exitIfError();
    }

    function exitIfError() {
        if (db.getLastErrorObj().err) {
            printjson(db.getLastErrorObj());
            throw "Exiting due to error above";
        }
    }
}});