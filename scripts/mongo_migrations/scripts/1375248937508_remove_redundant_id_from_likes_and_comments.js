/*
	Migrates likes and comments.
	Field 'id' is removed and value of '_id' is replaced with value of 'id'
*/
migrations.push({name: 'Remove redundant id fields from likes and comments', action: function(db) {

	var existingRecordsQuery = {id: {$exists: true}};

	migrateCollection('commit_likes');
	migrateCollection('commit_comments');

	function migrateCollection(collection) {
		var existingRecords = db[collection].find(existingRecordsQuery);
		if(!existingRecords.count()) {
			print("No items in " + collection + " to migrate - skipping");
			return;
		}
		print("Migrating " + existingRecords.count() + " records in " + collection);
		insertMigratedRecords(existingRecords, collection);
		removeOldRecords(collection);
		print("Done");	
	}

	function insertMigratedRecords(cursor, collection) {
		var newRecords = buildNewRecords(cursor);
		db[collection].insert(newRecords);		
		exitIfError();
	}

	function removeOldRecords(collection) {
		db[collection].remove(existingRecordsQuery);
		exitIfError();
	}

	function buildNewRecords(cursor) {
		return cursor.map(function(record) {
			record._id = record.id;	// rewrite id to _id
			delete record.id;	// remove id field
			return record;
		});
	}

	function exitIfError() {
		if(db.getLastErrorObj().err) {
			printjson(db.getLastErrorObj());
			throw "Exiting due to error above";
		}
	}
}});