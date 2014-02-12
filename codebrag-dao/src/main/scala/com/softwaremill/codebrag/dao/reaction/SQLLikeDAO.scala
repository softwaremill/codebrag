package com.softwaremill.codebrag.dao.reaction

import com.softwaremill.codebrag.dao.sql.{WithSQLSchemas, SQLDatabase}
import org.bson.types.ObjectId
import com.softwaremill.codebrag.domain.{Like, ThreadDetails}
import scala.slick.driver.JdbcProfile

class SQLLikeDAO(val database: SQLDatabase) extends LikeDAO with WithSQLSchemas with SQLReactionDAO {
  import database.driver.simple._
  import database._

  def save(like: Like) = db.withTransaction { implicit session =>
    likes += like
  }

  def findLikesForCommits(commitIds: ObjectId*): List[Like] = db.withTransaction { implicit session =>
    likes
      .filter(_.commitId inSet commitIds.toSet)
      .sortBy(_.postingTime.asc)
      .list()
  }

  def findAllLikesForThread(thread: ThreadDetails): List[Like] = db.withTransaction { implicit session =>
    likes
      .filter(c => c.commitId === thread.commitId && positionFilter(thread, c))
      .list()
  }

  def findById(likeId: ObjectId): Option[Like] = db.withTransaction { implicit session =>
    likes
      .filter(c => c.id === likeId)
      .firstOption()
  }

  def remove(likeId: ObjectId) {
    db.withTransaction { implicit session =>
      likes.filter(c => c.id === likeId).delete
    }
  }

  private class Likes(tag: Tag) extends Table[Like](tag, "likes") with ReactionTable {
    def * = (id, commitId, authorId, postingTime, fileName, lineNumber) <>
      (Like.tupled, Like.unapply)
  }

  private val likes = TableQuery[Likes]

  def schemas: Iterable[JdbcProfile#DDLInvoker] = List(likes.ddl)
}
