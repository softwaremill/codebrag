package com.softwaremill.codebrag.dao.reporting.views

import java.util.Date

case class CommentsView(comments: List[SingleCommentView], inlineComments: Map[String, Map[Int, List[SingleCommentView]]])
case class SingleCommentView(id: String, authorName: String, message: String, time: Date)

