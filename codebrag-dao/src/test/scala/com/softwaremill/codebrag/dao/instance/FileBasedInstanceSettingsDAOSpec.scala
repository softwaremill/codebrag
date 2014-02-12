package com.softwaremill.codebrag.dao.instance

import org.scalatest.{BeforeAndAfterEach, BeforeAndAfterAll, FlatSpec}
import java.util.UUID
import java.io.File
import org.scalatest.matchers.ShouldMatchers
import scala.io.Source

class FileBasedInstanceSettingsDAOSpec extends FlatSpec with BeforeAndAfterAll with BeforeAndAfterEach with ShouldMatchers {

  val testFileName = randomFileName

  val TestInstanceId = "abcde"
  val InvalidFileContent = "aaa" + System.getProperty("line.separator") + "bbb"

  override def afterEach() {
    removeTestFileIfExists()
  }


  it should "create file with instance ID inside if one doesn't exist" in {
    // given
    val settingsDao = new FileBasedInstanceSettingsDAO(testFileName)

    // when
    val Right(instanceSettings) = settingsDao.readOrCreate

    // then
    testFileShouldExist
    testFileShouldContainInstanceId(instanceSettings.uniqueId)
  }

  it should "read instanceId from existing file if one exist" in {
    // given
    createTestFileContainingId(TestInstanceId)
    val settingsDao = new FileBasedInstanceSettingsDAO(testFileName)

    // when
    val Right(instanceSettings) = settingsDao.readOrCreate

    // then
    instanceSettings.uniqueId should be(TestInstanceId)
  }

  it should "resolve to Left with message when file contains no instance ID" in {
    // given
    createTestFileContainingId("")
    val settingsDao = new FileBasedInstanceSettingsDAO(testFileName)

    // when
    val Left(msg) = settingsDao.readOrCreate

    // then
    msg should be("Cannot read instance ID")
  }

  it should "resolve to Left with message when file contains more than one line" in {
    // given
    createTestFileContainingId(InvalidFileContent)
    val settingsDao = new FileBasedInstanceSettingsDAO(testFileName)

    // when
    val Left(msg) = settingsDao.readOrCreate

    // then
    msg should be("Cannot read instance ID")
  }

  private def testFileShouldContainInstanceId(instanceId: String) {
    Source.fromFile(testFileName).getLines().toList.head should be(instanceId)
  }

  private def testFileShouldExist {
    new File(testFileName).exists() should be(true)
  }

  private def randomFileName = UUID.randomUUID().toString

  private def removeTestFileIfExists() {
    val fileCreated = new File(testFileName)
    if (fileCreated.exists()) {
      fileCreated.delete()
    }
  }

  private def createTestFileContainingId(instanceId: String) {
    scala.reflect.io.File(testFileName).writeAll(instanceId)
  }

}

