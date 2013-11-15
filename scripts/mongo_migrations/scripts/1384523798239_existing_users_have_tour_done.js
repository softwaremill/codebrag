/**
 * Adds {regular: true} field to all existing users' records
 * There will be internal users created and we need to distinguish between those two types
 */
migrations.push({name: 'Mark that existing users have app tour already done', action: function (db) {


    migrateCollectionUsers();



    function migrateCollectionUsers() {
        var users = usersToMigrate();
        if(users.count() == 0) {
            print('No records in collection "users" to migrate - skipping');
            return;
        }
        print('Migrating ' + users.count() + ' records in collection "users"');
        updateModifiedRecords(markTourDone(users));
        print('Done')
    }

    function usersToMigrate() {
        var query = {
            regular: true,
            'userSettings.appTourDone': {$exists: false}
        };
        return db['users'].find(query);
    }

    function markTourDone(records) {
        return records.map(function (record) {
            record.userSettings.appTourDone = true;
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