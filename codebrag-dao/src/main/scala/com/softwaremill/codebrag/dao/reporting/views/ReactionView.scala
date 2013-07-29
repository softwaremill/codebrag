package com.softwaremill.codebrag.dao.reporting.views

import java.util.Date

case class CommitReactionsView(entireCommitReactions: ReactionsView, inlineReactions: Map[String, Map[String, ReactionsView]])

case class ReactionsView(comments: Option[List[ReactionView]], likes: Option[List[ReactionView]])

trait ReactionView {
  def id: String
  def authorName: String
  def authorId: String
  def reactionType: String
  def time: Date
  def fileName: Option[String]
  def lineNumber: Option[Int]
}

case class LikeView(id: String, authorName: String, authorId: String, time: Date, fileName: Option[String] = None, lineNumber: Option[Int] = None) extends ReactionView {
  val reactionType = "like"
}

case class CommentView(id: String, authorName: String, authorId: String, message: String, time: Date, authorAvatarUrl: String = "", fileName: Option[String] = None, lineNumber: Option[Int] = None) extends ReactionView {
  val reactionType = "comment"
}