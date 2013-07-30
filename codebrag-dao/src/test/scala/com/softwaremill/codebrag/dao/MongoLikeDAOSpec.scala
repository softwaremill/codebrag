package com.softwaremill.codebrag.dao

import org.scalatest.matchers.ShouldMatchers
import com.softwaremill.codebrag.domain.Like
import com.softwaremill.codebrag.dao.ObjectIdTestUtils._
import org.bson.types.ObjectId
import com.foursquare.rogue.LiftRogue._
import com.softwaremill.codebrag.test.mongo.ClearDataAfterTest
import com.softwaremill.codebrag.builders.LikeAssembler._

class MongoLikeDAOSpec extends FlatSpecWithMongo with ClearDataAfterTest with ShouldMatchers {

  var likeDao: MongoLikeDAO = _

  val CommitId: ObjectId = oid(2)
  val AnotherCommitId: ObjectId = oid(123)

  override def beforeEach() {
    super.beforeEach()
    likeDao = new MongoLikeDAO
  }

  it should "store new like for entire commit" taggedAs (RequiresDb) in {
    val newLike = likeFor(CommitId).get

    // when
    likeDao.save(newLike)
    val likes = LikeRecord.where(_.id eqs newLike.id).and(_.commitId eqs newLike.commitId).fetch()

    // then
    likes.size should be(1)
    likes.head.commitId.get should be(CommitId)
  }

  it should "store new line like for commit" taggedAs (RequiresDb) in {
    val lineLike = likeFor(CommitId).withFileNameAndLineNumber("file.txt", 10).get

    // when
    likeDao.save(lineLike)
    val likes = LikeRecord.where(_.id eqs lineLike.id).and(_.commitId eqs lineLike.commitId).fetch()

    // then
    likes.size should be(1)
    val savedLike = likes.head
    savedLike.fileName.get should equal(lineLike.fileName)
    savedLike.lineNumber.get should equal(lineLike.lineNumber)
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

  it should "load only likes for commit id" taggedAs (RequiresDb) in {
    // given
    val fixtureLikesList = createLikesFor(CommitId, 3)
    val additionalLikes = createLikesFor(AnotherCommitId, 5)
    fixtureLikesList.foreach(likeDao.save(_))
    additionalLikes.foreach(likeDao.save(_))

    // when
    val likes = likeDao.findLikesForCommit(CommitId)

    // then
    likes should equal(fixtureLikesList)
  }

  it should "find inline likes and likes for entire commit" taggedAs (RequiresDb) in {
    // given
    val like = likeFor(CommitId).get
    val inlineLike = likeFor(CommitId).withFileNameAndLineNumber("text.txt", 10).get
    likeDao.save(like)
    likeDao.save(inlineLike)

    // when
    val likes = likeDao.findLikesForCommit(CommitId)

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
    val inlineLikesRelated = likeDao.findAllLikesInThreadWith(firstInlineLike)
    val generalLikesRelated = likeDao.findAllLikesInThreadWith(commitLikes.head)

    // then
    inlineLikesRelated.toSet should be(Set(firstInlineLike, secondInlineLike))
    generalLikesRelated.toSet should be(commitLikes.toSet)
  }


  private def createLikesFor(commitId: ObjectId, howMany: Int): Seq[Like] = {
    (1 to howMany).map(i => likeFor(commitId).get).toSeq
  }
}
