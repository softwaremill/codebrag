package com.softwaremill.codebrag.service.comments.command

import org.bson.types.ObjectId

case class IncomingComment(commitId: ObjectId, authorId: ObjectId, message: String, fileName: Option[String] = None, lineNumber: Option[Int] = None)
