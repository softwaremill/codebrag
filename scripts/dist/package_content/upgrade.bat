echo "Upgrading Codebrag 1.0 to 1.2"
java -Dconfig.file=codebrag.conf -cp codebrag.jar com.softwaremill.codebrag.dao.sql.MigrateMongoToSQL