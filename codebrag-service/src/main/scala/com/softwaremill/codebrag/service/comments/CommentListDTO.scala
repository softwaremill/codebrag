package com.softwaremill.codebrag.service.comments

import org.joda.time.DateTime

case class CommentListDTO(comments: List[CommentListItemDTO])
case class CommentListItemDTO(id: String, authorName: String, message: String, time: DateTime)
