package com.softwaremill.codebrag.dao.reporting

import java.util.Date

case class CommentListDTO(comments: List[CommentListItemDTO])
case class CommentListItemDTO(id: String, authorName: String, message: String, time: Date)

/* new versions of transfer objects for comments */
case class CommentsView(comments: List[SingleCommentView], inlineComments: List[FileCommentsView])
case class FileCommentsView(fileName: String, lineComments: List[LineCommentsView])
case class LineCommentsView(lineNumber: Int, comments: List[SingleCommentView])
case class SingleCommentView(id: String, authorName: String, message: String, time: Date)
