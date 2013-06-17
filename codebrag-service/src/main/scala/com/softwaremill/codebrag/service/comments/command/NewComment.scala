package com.softwaremill.codebrag.service.comments.command

import org.bson.types.ObjectId

trait IncomingUserReaction {

  def commitId: ObjectId
  def authorId: ObjectId
  def fileName: Option[String]
  def lineNumber: Option[Int]

}

case class IncomingComment(commitId: ObjectId, authorId: ObjectId, message: String, fileName: Option[String] = None, lineNumber: Option[Int] = None) extends IncomingUserReaction

case class IncomingLike(commitId: ObjectId, authorId: ObjectId, fileName: Option[String] = None, lineNumber: Option[Int] = None) extends IncomingUserReaction
