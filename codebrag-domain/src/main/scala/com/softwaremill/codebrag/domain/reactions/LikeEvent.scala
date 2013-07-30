package com.softwaremill.codebrag.domain.reactions

import com.softwaremill.codebrag.common.Event
import com.softwaremill.codebrag.domain.Like
import org.bson.types.ObjectId

case class LikeEvent(like: Like) extends Event

case class UnlikeEvent(likeId: ObjectId) extends Event
