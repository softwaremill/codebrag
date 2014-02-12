package com.softwaremill.codebrag.dao.finders.reaction

import com.softwaremill.codebrag.dao.ObjectIdTestUtils
import org.scalatest.matchers.ShouldMatchers
import com.softwaremill.codebrag.domain.builder.{LikeAssembler, UserAssembler}
import com.softwaremill.codebrag.dao.user.MongoUserDAO
import com.softwaremill.codebrag.test.{FlatSpecWithMongo, ClearMongoDataAfterTest}
import com.softwaremill.codebrag.dao.reaction.MongoLikeDAO
import com.softwaremill.codebrag.dao.finders.reaction.LikesFinder

class LikesFinderSpec extends FlatSpecWithMongo with ClearMongoDataAfterTest with ShouldMatchers {

  val likesFinder = new LikesFinder {}
  val userDao = new MongoUserDAO
  val likesDao = new MongoLikeDAO

  val user = UserAssembler.randomUser.withFullName("John Doe").get
  val commitId = ObjectIdTestUtils.oid(100)
  val nonExistingAuthorId = ObjectIdTestUtils.oid(10)

  override def beforeEach() {
    userDao.add(user)
  }

  it should "find like by id" in {
    // given
    val like = LikeAssembler.likeFor(commitId).withAuthorId(user.id).get
    likesDao.save(like)

    // when
    val found = likesFinder.findLikeById(like.id)

    found.get.id should equal(like.id.toString)
    found.get.authorId should equal(user.id.toString)
    found.get.authorName should equal(user.name)
  }

  it should "find like by id with empty user name when like author not found" in {
    // given
    val like = LikeAssembler.likeFor(commitId).withAuthorId(nonExistingAuthorId).get
    likesDao.save(like)

    // when
    val found = likesFinder.findLikeById(like.id)

    found.get.id should equal(like.id.toString)
    found.get.authorId should equal(nonExistingAuthorId.toString)
    found.get.authorName should be('empty)
  }

  it should "return None if like not found" in {
    // given
    val nonExistingLikeId = ObjectIdTestUtils.oid(200)

    // when
    val found = likesFinder.findLikeById(nonExistingLikeId)

    found should be('empty)
  }

}
