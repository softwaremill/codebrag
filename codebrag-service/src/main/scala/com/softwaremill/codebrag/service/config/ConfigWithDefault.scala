package com.softwaremill.codebrag.service.config

import com.typesafe.config.Config

trait ConfigWithDefault {

  def rootConfig: Config

  def getBoolean(path: String, default: Boolean) = ifHasPath(path, default) {_.getBoolean(path)}
  def getString(path: String, default:String) =  ifHasPath(path,default) { _.getString(path) }
  def getInt(path: String, default:Int) =  ifHasPath(path,default) { _.getInt(path) }
  def getConfig(path: String, default:Config) =  ifHasPath(path,default) { _.getConfig(path) }

  private def ifHasPath[T](path: String, default:T)(get: Config => T): T = {
    if (rootConfig.hasPath(path)) get(rootConfig) else default
  }

}
