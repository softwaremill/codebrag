package com.softwaremill.codebrag.dao

import com.softwaremill.codebrag.domain.Like
import org.bson.types.ObjectId

trait LikeDAO {

  def save(like: Like)

  def findLikesForCommit(commitId: ObjectId): List[Like]

  def findAllLikesInThreadWith(comment: Like): List[Like]

  def findById(likeId: ObjectId): Option[Like]

  def remove(likeId: ObjectId)

}
