package com.softwaremill.codebrag.common.config

import com.typesafe.config.Config
import scala.collection.JavaConversions._

trait ConfigWithDefault {

  def rootConfig: Config

  def getBoolean(path: String, default: Boolean) = ifHasPath(path, default) {_.getBoolean(path)}
  def getString(path: String, default:String) =  ifHasPath(path,default) { _.getString(path) }
  def getInt(path: String, default:Int) =  ifHasPath(path,default) { _.getInt(path) }
  def getConfig(path: String, default:Config) =  ifHasPath(path,default) { _.getConfig(path) }
  def getMilliseconds(path:String, default:Long) = ifHasPath(path, default) { _.getMilliseconds(path) }
  def getOptionalString(path: String, default: Option[String] = None) = getOptional(path) { _.getString(path) }
  def getStringList(path: String, default: List[String] = List()) = ifHasPath(path, default) { _.getStringList(path).toList }

  private def ifHasPath[T](path: String, default:T)(get: Config => T): T = {
    if (rootConfig.hasPath(path)) get(rootConfig) else default
  }

  private def getOptional[T](fullPath: String, default: Option[T] = None)(get: Config => T) = {
    if(rootConfig.hasPath(fullPath)) {
      Some(get(rootConfig))
    } else {
      default
    }
  }


}
