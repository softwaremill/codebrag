package com.softwaremill.codebrag.dao

import com.typesafe.scalalogging.slf4j.Logging
import java.io.File
import scala.io.Source
import com.softwaremill.codebrag.domain.InstanceSettings
import org.bson.types.ObjectId

class FileBasedInstanceSettingsDAO(fileName: String = ".instanceId") extends InstanceSettingsDAO with Logging {

  def readOrCreate = {
    val instanceFile = new File(fileName)
    if(instanceFile.exists()) {
      logger.debug("Reading instance ID from file")
      readInstanceId
    } else {
      logger.debug("No instance ID found - creating new")
      createAndReturnInstanceId
    }
  }

  private def readInstanceId = {
    Source.fromFile(fileName).getLines().toList match {
      case List(singleLine) => Right(InstanceSettings(singleLine))
      case _ => Left("Cannot read instance ID")
    }
  }

  private def createAndReturnInstanceId = {
    val newInstanceId = new ObjectId().toString
    scala.reflect.io.File(fileName).writeAll(newInstanceId)
    Right(InstanceSettings(newInstanceId))
  }
}