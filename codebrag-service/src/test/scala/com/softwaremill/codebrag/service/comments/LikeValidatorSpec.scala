package com.softwaremill.codebrag.service.comments

import org.scalatest.{BeforeAndAfterEach, FlatSpec}
import org.scalatest.mock.MockitoSugar
import org.scalatest.matchers.ShouldMatchers
import com.softwaremill.codebrag.dao.{ObjectIdTestUtils, LikeDAO}
import com.softwaremill.codebrag.domain.Like
import org.bson.types.ObjectId
import org.joda.time.DateTime
import com.softwaremill.codebrag.dao.user.UserDAO
import com.softwaremill.codebrag.dao.commitinfo.CommitInfoDAO

class LikeValidatorSpec extends FlatSpec with MockitoSugar with ShouldMatchers with BeforeAndAfterEach{

  var commitDaoDummy: CommitInfoDAO = _
  var likeDaoDummy: LikeDAO = _
  var userDaoDummy: UserDAO = _

  var likeValidator: LikeValidator with TestUserIsCommitAuthorCheck with TestUserAlreadyLikedIt = _

  val LikeAuthorId = ObjectIdTestUtils.oid(100)
  val CommitId = ObjectIdTestUtils.oid(200)

  val LikeToValidate = Like(new ObjectId, CommitId, LikeAuthorId, DateTime.now, Some("file.txt"), Some(20))

  override def beforeEach() {
    likeValidator = new LikeValidator(commitDaoDummy, likeDaoDummy, userDaoDummy) with TestUserIsCommitAuthorCheck with TestUserAlreadyLikedIt
  }

  it should "mark like as valid" in {
    // given
    likeValidator.userIsCommitAuthor = false
    likeValidator.userAlreadyLikedIt = false

    // when
    val result = likeValidator.isLikeValid(LikeToValidate)

    // then
    result should be(Right())
  }

  it should "mark like as invalid when there is another like of given user for given code" in {
    // given
    likeValidator.userAlreadyLikedIt = true

    // when
    val Left(result) = likeValidator.isLikeValid(LikeToValidate)

    // then
    result should be(LikeValidator.UserCantLikeMultipleTimes)
  }

  it should "mark like as invalid when user is commit author" in {
    // given
    likeValidator.userIsCommitAuthor = true

    // when
    val Left(result) = likeValidator.isLikeValid(LikeToValidate)

    // then
    result should be(LikeValidator.UserCantLikeOwnCode)
  }

}

trait TestUserIsCommitAuthorCheck extends UserIsCommitAuthorCheck {

  override val commitDao: CommitInfoDAO  = null // FIXME: null? Bleeee
  override val userDao: UserDAO = null

  var userIsCommitAuthor: Boolean = false

  override def userIsCommitAuthor(like: Like) = userIsCommitAuthor

}

trait TestUserAlreadyLikedIt extends UserAlreadyLikedItCheck {

  override val likeDao: LikeDAO = null

  var userAlreadyLikedIt: Boolean = false

  override def userAlreadyLikedThat(like: Like) = userAlreadyLikedIt

}



