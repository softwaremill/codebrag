package com.softwaremill.codebrag.service.comments

import java.util.Date

case class CommentListDTO(comments: List[CommentListItemDTO])
case class CommentListItemDTO(id: String, authorName: String, message: String, time: Date)
