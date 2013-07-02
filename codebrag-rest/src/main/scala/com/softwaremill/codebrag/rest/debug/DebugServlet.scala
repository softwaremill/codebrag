package com.softwaremill.codebrag.rest.debug

import com.softwaremill.codebrag.rest.JsonServlet
import com.softwaremill.codebrag.service.commits.{RepoDataProducer, CommitImportService}
import com.softwaremill.codebrag.service.config.CodebragConfig
import scala.sys.process.Process
import org.apache.commons.lang3.exception.ExceptionUtils

class DebugServlet(repoDataProducer: RepoDataProducer,
                   commitImportService: CommitImportService,
                   configuration: CodebragConfig)
  extends JsonServlet with DebugBasicAuthSupport {

  override def login = configuration.debugServicesLogin
  override def password = configuration.debugServicesPassword

  get("/resetAll") {
    basicAuth()
    val homeDir = System.getProperty("user.home")
    try
    {
    Process("./resetAll.sh", new java.io.File(homeDir)).!
    "Reset successfull."
    }
    catch {
      case exception: Throwable => ExceptionUtils.getStackTrace(exception)
    }
  }

}

object DebugServlet {
  val MappingPath = "debug"
}
