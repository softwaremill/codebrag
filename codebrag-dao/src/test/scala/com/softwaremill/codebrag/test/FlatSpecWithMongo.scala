package com.softwaremill.codebrag.test

import org.scalatest.{BeforeAndAfterEach, BeforeAndAfterAll, FlatSpec}
import com.mongodb.Mongo
import com.typesafe.config.Config
import com.softwaremill.codebrag.dao.mongo.{MongoConfig, MongoInit}
import com.softwaremill.codebrag.test.MongoRunner

trait FlatSpecWithMongo extends FlatSpec with BeforeAndAfterAll with BeforeAndAfterEach {

  object TestMongoConfig extends MongoConfig {

    def rootConfig: Config = null

    val MongoPort = 24567
    val MongoHost = "localhost"

    override lazy val mongoServers: String = s"$MongoHost:$MongoPort"
    override lazy val mongoDatabase: String = "codebrag_test"

  }

  protected var mongoRunner: MongoRunner = null

  override protected def beforeAll() {
    super.beforeAll()
    startMongo()
  }

  def clearData() {
    import scala.collection.JavaConverters._
    val mongo = new Mongo(mongoRunner.serverAddress())
    val dbNames = mongo.getDatabaseNames.asScala
    for (dbName <- dbNames) {
      mongo.getDB(dbName).dropDatabase()
    }
    mongo.close()
  }

  override protected def afterAll() {
    stopMongo()
    super.afterAll()
  }

  def startMongo() {
    mongoRunner = MongoRunner.run(TestMongoConfig.MongoPort, verbose = true)
    MongoInit.initialize(TestMongoConfig)
  }

  def stopMongo() {
    mongoRunner.stop()
  }
}