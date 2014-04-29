package com.softwaremill.codebrag.instance

import com.softwaremill.codebrag.dao.instance.InstanceParamsDAO
import com.softwaremill.codebrag.domain.{InstanceParam, InstanceId}
import com.typesafe.scalalogging.slf4j.Logging

class InstanceParamsService(instanceParamsDAO: InstanceParamsDAO) extends Logging {

  def readOrCreateInstanceId: InstanceId = {
    instanceParamsDAO.findByKey(InstanceId.Key) match {
      case Some(param) => {
        logger.debug(s"Instance ID found, returning ${param.value}")
        InstanceId(param.value)
      }
      case None => {
        val newId = InstanceId.createNew
        logger.debug(s"Instance ID not found, creating new ${newId.value} and returning it")
        instanceParamsDAO.save(newId.toInstanceParam)
        newId
      }
    }
  }

  def save(param: InstanceParam) = instanceParamsDAO.save(param)
  
}
