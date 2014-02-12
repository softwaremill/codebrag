package com.softwaremill.codebrag.dao.reaction

import com.softwaremill.codebrag.domain.{ThreadDetails, Like}
import org.bson.types.ObjectId

trait LikeDAO {

  def save(like: Like)

  def findLikesForCommits(commitIds: ObjectId*): List[Like]

  def findAllLikesForThread(thread: ThreadDetails): List[Like]

  def findById(likeId: ObjectId): Option[Like]

  def remove(likeId: ObjectId)

}
