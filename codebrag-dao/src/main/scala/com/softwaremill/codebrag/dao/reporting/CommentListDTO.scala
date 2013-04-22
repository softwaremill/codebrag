package com.softwaremill.codebrag.dao.reporting

import java.util.Date

case class CommentListDTO(comments: List[CommentListItemDTO])
case class CommentListItemDTO(id: String, authorName: String, message: String, time: Date)

/* new versions of transfer objects for comments */
case class Comments(comments: List[GeneralComment], inlineComments: List[FileComments])
case class FileComments(fileName: String, lineComments: List[LineComments])
case class LineComments(lineNumber: Int, comments: List[GeneralComment])
case class GeneralComment(id: String, authorName: String, message: String, time: Date)
