package com.softwaremill.codebrag.dao.reaction

import org.scalatest.matchers.ShouldMatchers
import com.softwaremill.codebrag.domain.Like
import com.softwaremill.codebrag.dao.ObjectIdTestUtils._
import org.bson.types.ObjectId
import com.softwaremill.codebrag.domain.builder.LikeAssembler
import LikeAssembler._
import com.softwaremill.codebrag.test.{ClearSQLDataAfterTest, FlatSpecWithSQL, FlatSpecWithMongo, ClearMongoDataAfterTest}
import com.softwaremill.codebrag.dao.RequiresDb
import org.scalatest.FlatSpec

trait LikeDAOSpec extends FlatSpec with ShouldMatchers {
  def likeDao: LikeDAO

  val CommitId: ObjectId = oid(2)
  val AnotherCommitId: ObjectId = oid(123)
  val YetAnotherCommitId: ObjectId = oid(456)

  it should "store new like for entire commit" taggedAs (RequiresDb) in {
    val newLike = likeFor(CommitId).get

    // when
    likeDao.save(newLike)
    val likes = likeDao.findLikesForCommits(newLike.commitId)

    // then
    likes.size should be(1)
    likes.head.authorId should be(newLike.authorId)
    likes.head.commitId should be(CommitId)
  }

  it should "store new line like for commit" taggedAs (RequiresDb) in {
    val lineLike = likeFor(CommitId).withFileNameAndLineNumber("file.txt", 10).get

    // when
    likeDao.save(lineLike)
    val likes = likeDao.findLikesForCommits(lineLike.commitId)

    // then
    likes.size should be(1)
    val savedLike = likes.head
    savedLike.fileName should equal(lineLike.fileName)
    savedLike.lineNumber should equal(lineLike.lineNumber)
  }

  it should "load like by id if one exists" taggedAs (RequiresDb) in {
    //given
    val like = likeFor(CommitId).get
    likeDao.save(like)

    // when
    val Some(found) = likeDao.findById(like.id)

    // then
    found should equal(like)
  }

  it should "remove like by given id" taggedAs(RequiresDb) in {
    //given
    val like = likeFor(CommitId).get
    likeDao.save(like)

    // when
    likeDao.remove(like.id)

    // then
    likeDao.findById(like.id) should be('empty)
  }

  it should "load only likes for given commits" taggedAs (RequiresDb) in {
    // given
    val fixtureLikesList = createLikesFor(CommitId, 3)
    val additionalLikes = createLikesFor(AnotherCommitId, 5)
    val yetAnotherLikes = createLikesFor(YetAnotherCommitId, 5)
    fixtureLikesList.foreach(likeDao.save(_))
    additionalLikes.foreach(likeDao.save(_))
    yetAnotherLikes.foreach(likeDao.save(_))

    // when
    val likes = likeDao.findLikesForCommits(CommitId, AnotherCommitId)

    // then
    likes should equal(fixtureLikesList ++ additionalLikes)
  }

  it should "find inline likes and likes for entire commit" taggedAs (RequiresDb) in {
    // given
    val like = likeFor(CommitId).get
    val inlineLike = likeFor(CommitId).withFileNameAndLineNumber("text.txt", 10).get
    likeDao.save(like)
    likeDao.save(inlineLike)

    // when
    val likes = likeDao.findLikesForCommits(CommitId)

    // then
    likes.length should be(2)
    likes.map(_.id).toSet should equal(Set(like.id, inlineLike.id))
  }

  it should "find all likes in thread containing given like (general or for the same file and line)" taggedAs(RequiresDb) in {
    // given general comments
    val commitLikes = createLikesFor(CommitId, 3)
    commitLikes.foreach(likeDao.save)

    // and some inline comments
    val firstInlineLike = likeFor(CommitId).withFileNameAndLineNumber("file_1.txt", 10).get
    val secondInlineLike = likeFor(CommitId).withFileNameAndLineNumber("file_1.txt", 10).get
    val anotherInlineLike = likeFor(CommitId).withFileNameAndLineNumber("file_2.txt", 20).get
    likeDao.save(firstInlineLike)
    likeDao.save(secondInlineLike)
    likeDao.save(anotherInlineLike)

    // when
    val inlineLikesRelated = likeDao.findAllLikesForThread(firstInlineLike.threadId)
    val generalLikesRelated = likeDao.findAllLikesForThread(commitLikes.head.threadId)

    // then
    inlineLikesRelated.toSet should be(Set(firstInlineLike, secondInlineLike))
    generalLikesRelated.toSet should be(commitLikes.toSet)
  }


  private def createLikesFor(commitId: ObjectId, howMany: Int): Seq[Like] = {
    (1 to howMany).map(i => likeFor(commitId).get).toSeq
  }
}

class MongoLikeDAOSpec extends FlatSpecWithMongo with ClearMongoDataAfterTest with LikeDAOSpec {
  val likeDao = new MongoLikeDAO()
}

class SQLLikeDAOSpec extends FlatSpecWithSQL with ClearSQLDataAfterTest with LikeDAOSpec {
  val likeDao = new SQLLikeDAO(sqlDatabase)

  def withSchemas = List(likeDao)
}