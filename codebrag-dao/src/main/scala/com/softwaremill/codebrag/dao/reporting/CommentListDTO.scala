package com.softwaremill.codebrag.dao.reporting

import java.util.Date

case class CommentsView(comments: List[SingleCommentView], inlineComments: List[FileCommentsView])
case class FileCommentsView(fileName: String, lineComments: List[LineCommentsView])
case class LineCommentsView(lineNumber: Int, comments: List[SingleCommentView])
case class SingleCommentView(id: String, authorName: String, message: String, time: Date)
