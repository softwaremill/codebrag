echo "Upgrading Codebrag 2.0 to 2.1"
java -Dconfig.file=codebrag.conf -cp codebrag.jar com.softwaremill.codebrag.migration.MigrateV2_1ToV2_2