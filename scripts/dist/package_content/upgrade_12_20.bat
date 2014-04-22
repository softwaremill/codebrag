echo "Upgrading Codebrag 1.2 to 2.0"
java -Dconfig.file=codebrag.conf -cp codebrag.jar com.softwaremill.codebrag.dao.sql.MigrateV1_2ToV2_0