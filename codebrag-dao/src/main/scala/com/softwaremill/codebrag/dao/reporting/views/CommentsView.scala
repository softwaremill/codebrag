package com.softwaremill.codebrag.dao.reporting.views

import java.util.Date

object CommentsView {
  type LineToCommentListMap = Map[Int, List[SingleCommentView]]
}

case class CommentsView(comments: List[SingleCommentView], inlineComments: Map[String, CommentsView.LineToCommentListMap])
case class SingleCommentView(id: String, authorName: String, message: String, time: Date)

