echo "Upgrading Codebrag 2.2 to 2.3"
java -Dconfig.file=codebrag.conf -cp codebrag.jar com.softwaremill.codebrag.migration.MigrateV2_2ToV2_3
java -Dconfig.file=codebrag.conf -cp codebrag.jar com.softwaremill.codebrag.migration.FixZombieCommits