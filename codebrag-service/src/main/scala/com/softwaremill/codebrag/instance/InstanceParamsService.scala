package com.softwaremill.codebrag.instance

import com.softwaremill.codebrag.dao.instance.InstanceParamsDAO
import com.softwaremill.codebrag.domain.{InstanceParam, InstanceId}
import com.typesafe.scalalogging.slf4j.Logging
import scala.io.Source
import java.io.File

class InstanceParamsService(instanceParamsDAO: InstanceParamsDAO) extends ImportFileBasedInstanceId with Logging {

  def readOrCreateInstanceId: InstanceId = {
    try {
      doReadOrCreateInstanceId
    } catch {
      case e: Exception => throw new RuntimeException("Cannot properly initialize instance ID", e)
    }
  }

  def save(param: InstanceParam) = instanceParamsDAO.save(param)

  private def doReadOrCreateInstanceId = {
    instanceParamsDAO.findByKey(InstanceId.Key) match {
      case Some(param) => returnExistingInstanceId(param)        
      case None => {
        loadExistingInstanceIdFromFile() match {
          case Some(existingId) => migrateInstanceIdToDatabaseAndReturn(existingId)
          case None => createAndReturnNewInstanceId()            
        }
      }
    }
  }


  private def createAndReturnNewInstanceId(): InstanceId = {
    val newId = InstanceId.createNew
    logger.debug(s"Instance ID not found, creating new ${newId.value} and returning it")
    instanceParamsDAO.save(newId.toInstanceParam)
    newId
  }

  private def migrateInstanceIdToDatabaseAndReturn(existingId: InstanceId): InstanceId = {
    logger.debug(s"Instance ID found in file, migrating to DB ${existingId.value} and returning it")
    instanceParamsDAO.save(existingId.toInstanceParam)
    removeInstanceIdFile
    existingId
  }

  private def returnExistingInstanceId(param: InstanceParam): InstanceId = {
    logger.debug(s"Instance ID found, returning ${param.value}")
    InstanceId(param.value)
  }
}

trait ImportFileBasedInstanceId extends Logging {

  private val InstanceIdFilename = ".instanceId"

  protected def loadExistingInstanceIdFromFile(): Option[InstanceId] = {
    if (instanceIdFile.isDefined) {
      Source.fromFile(InstanceIdFilename).getLines().toList match {
        case List(singleLine) => Some(InstanceId(singleLine))
        case _ => None
      }
    } else {
      None
    }
  }
  
  private def instanceIdFile = Option(new File(InstanceIdFilename))

  protected def removeInstanceIdFile = {
    try {
      instanceIdFile.foreach(_.delete)      
    } catch {
      case e: Exception => logger.warn("Cannot remove old instance file. Leaving it in place.")
    }
  }

}