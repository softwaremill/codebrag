echo "Upgrading Codebrag 2.1 to 2.2"
java -Dconfig.file=codebrag.conf -cp codebrag.jar com.softwaremill.codebrag.migration.MigrateV2_1ToV2_2