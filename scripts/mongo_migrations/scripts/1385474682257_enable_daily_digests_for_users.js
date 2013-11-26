/**
 * Adds {dailyUpdatesEmailEnabled: true} field to all existing users' records
 */
migrations.push({name: 'Enable daily updates emails for all users', action: function (db) {


    migrateCollectionUsers();



    function migrateCollectionUsers() {
        var users = usersToMigrate();
        if(users.count() == 0) {
            print('No records in collection "users" to migrate - skipping');
            return;
        }
        print('Migrating ' + users.count() + ' records in collection "users"');
        updateModifiedRecords(setDailyUpdatesEnabled(users));
        print('Done')
    }

    function usersToMigrate() {
        var query = {
            regular: true,
            'userSettings.dailyUpdatesEmailEnabled': {$exists: false}
        };
        return db['users'].find(query);
    }

    function setDailyUpdatesEnabled(records) {
        return records.map(function (record) {
            record.userSettings.dailyUpdatesEmailEnabled = true;
            return record;
        });
    }

    function updateModifiedRecords(records) {
        records.forEach(function(record) {
            db.users.update({_id: record._id}, record);
            exitIfError();
        })
    }

    function exitIfError() {
        if (db.getLastErrorObj().err) {
            printjson(db.getLastErrorObj());
            throw "Exiting due to error above";
        }
    }
}});