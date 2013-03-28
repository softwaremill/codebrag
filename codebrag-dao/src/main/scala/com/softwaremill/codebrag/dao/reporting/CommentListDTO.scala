package com.softwaremill.codebrag.dao.reporting

import java.util.Date

case class CommentListDTO(comments: List[CommentListItemDTO])
case class CommentListItemDTO(id: String, authorName: String, message: String, time: Date)
