package com.softwaremill.codebrag.eventstream

import com.softwaremill.codebrag.domain.reactions.{UnlikeEvent, LikeEvent}
import com.softwaremill.codebrag.domain._
import org.json4s.ext.JodaTimeSerializers
import org.json4s.mongo.ObjectIdSerializer
import org.json4s.{DefaultFormats, FieldSerializer}
import org.json4s.FieldSerializer._

object InternalSerializers {

  val LikeSerializer = FieldSerializer[Like](
    ignore("id") orElse
    ignore("commitId") orElse
    ignore("authorId") orElse
    ignore("reactionType")
  )

  val UserSerializer = FieldSerializer[User](
    ignore("id") orElse
    ignore("authentication") orElse
    ignore("token") orElse
    ignore("settings") orElse
    ignore("notifications") orElse
    ignore("active") orElse
    ignore("admin")
  )

  val CommitInfoSerializer = FieldSerializer[CommitInfo](ignore("id"))

  val PartialCommitInfoSerializer = FieldSerializer[PartialCommitInfo](ignore("id"))

  val CommentSerializer = FieldSerializer[Comment](
    ignore("id") orElse
    ignore("commitId") orElse
    ignore("authorId") orElse
    ignore("reactionType")
  )

  val LikeEventSerializer = FieldSerializer[LikeEvent](ignore("clock"))

  val UnlikeEventSerializer = FieldSerializer[UnlikeEvent](ignore("clock"))

  val all =
    DefaultFormats +
    new ObjectIdSerializer() +
    FieldSerializer[Hook]() +
    LikeSerializer +
    UserSerializer +
    CommitInfoSerializer +
    PartialCommitInfoSerializer +
    CommentSerializer +
    LikeEventSerializer +
    UnlikeEventSerializer ++
    JodaTimeSerializers.all

}
