package com.softwaremill.codebrag.rest

trait UserReactionParametersReader {

  self: JsonServlet =>

  def readReactionParamsFromRequest = {
    val fileNameOpt = (parsedBody \ "fileName").extractOpt[String]
    val lineNumberOpt = (parsedBody \ "lineNumber").extractOpt[Int]
    val commitIdParam = params("id")
    if(fileNameOpt.isDefined ^ lineNumberOpt.isDefined) {
      halt(400, "File name and line number must be present for inline comment")
    }
    CommonReactionRequestParams(commitIdParam, fileNameOpt, lineNumberOpt)
  }

  case class CommonReactionRequestParams(commitId: String, fileName: Option[String], lineNumber: Option[Int])
}
