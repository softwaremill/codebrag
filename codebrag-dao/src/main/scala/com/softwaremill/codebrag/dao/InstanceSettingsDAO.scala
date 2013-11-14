package com.softwaremill.codebrag.dao

import com.softwaremill.codebrag.domain.InstanceSettings

trait InstanceSettingsDAO {

  /**
   * Reads instance settings from storage, if instance doesn't exist creates new one with random uniqueId
   *
   * @return either error message or instance of class Instance
   */
  def readOrCreate: Either[String, InstanceSettings]

}
