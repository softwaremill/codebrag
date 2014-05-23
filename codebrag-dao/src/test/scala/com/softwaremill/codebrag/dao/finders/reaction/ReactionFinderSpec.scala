package com.softwaremill.codebrag.dao.finders.reaction

import com.softwaremill.codebrag.dao.ObjectIdTestUtils._
import com.softwaremill.codebrag.dao._
import org.scalatest.matchers.ShouldMatchers
import com.softwaremill.codebrag.domain.{LastUserNotificationDispatch, UserSettings, Authentication, User}
import org.joda.time.DateTime
import com.softwaremill.codebrag.domain.builder.{UserAssembler, LikeAssembler, CommentAssembler}
import org.scalatest.BeforeAndAfterEach
import scala.Some
import com.softwaremill.codebrag.test.{ClearSQLDataAfterTest, FlatSpecWithSQL}
import com.softwaremill.codebrag.dao.finders.views.{CommentView, ReactionsView, CommitReactionsView, ReactionView}
import com.softwaremill.codebrag.dao.user.SQLUserDAO
import com.softwaremill.codebrag.dao.reaction.{SQLLikeDAO, SQLCommitCommentDAO}

class ReactionFinderSpec extends FlatSpecWithSQL with ClearSQLDataAfterTest with ShouldMatchers
  with ReactionFinderVerifyHelpers with BeforeAndAfterEach {

  val userDao = new SQLUserDAO(sqlDatabase)
  val commentDao = new SQLCommitCommentDAO(sqlDatabase)
  val likeDao = new SQLLikeDAO(sqlDatabase)

  val reactionsFinder = new ReactionFinder(userDao, commentDao, likeDao)

  val CommitId = oid(1)

  val John = UserAssembler.randomUser.withBasicAuth("john", "pass").withFullName("John").withEmail("john@doe.com").withToken("123123").get
  val Mary = UserAssembler.randomUser.withBasicAuth("mary", "pass").withFullName("Mary").withEmail("mary@doe.com").withToken("abcabc").get

  val user = UserAssembler.randomUser.withFullName("John Doe").get
  val commitId = ObjectIdTestUtils.oid(100)
  val nonExistingAuthorId = ObjectIdTestUtils.oid(10)

  val StoredCommitComments = List(
    CommentAssembler.commentFor(CommitId).withAuthorId(John.id).withMessage("Monster class").get,
    CommentAssembler.commentFor(CommitId).withAuthorId(Mary.id).withMessage("Fix it ASAP").get
  )

  val StoredInlineComments = List(
    CommentAssembler.commentFor(CommitId).withFileNameAndLineNumber("Main.scala", 10).withMessage("Cool thing").withAuthorId(John.id).get,
    CommentAssembler.commentFor(CommitId).withFileNameAndLineNumber("Main.scala", 10).withMessage("Indeed").withAuthorId(Mary.id).get,
    CommentAssembler.commentFor(CommitId).withFileNameAndLineNumber("Database.scala", 12).withMessage("Possible NPE?").withAuthorId(Mary.id).get,
    CommentAssembler.commentFor(CommitId).withFileNameAndLineNumber("Database.scala", 12).withMessage("Nope").withAuthorId(John.id).get,
    CommentAssembler.commentFor(CommitId).withFileNameAndLineNumber("Database.scala", 20).withMessage("Refactor that").withAuthorId(John.id).get
  )

  val StoredInlineLikes = List(
    LikeAssembler.likeFor(CommitId).withFileNameAndLineNumber("Main.scala", 10).withAuthorId(John.id).get,
    LikeAssembler.likeFor(CommitId).withFileNameAndLineNumber("Main.scala", 10).withAuthorId(Mary.id).get,
    LikeAssembler.likeFor(CommitId).withFileNameAndLineNumber("Database.scala", 12).withAuthorId(Mary.id).get
  )

  override def beforeEach() {
    super.beforeEach()

    StoredCommitComments.foreach(commentDao.save)
    StoredInlineComments.foreach(commentDao.save)

    StoredInlineLikes.foreach(likeDao.save)

    List(John, Mary).foreach(userDao.add)

    userDao.add(user)
  }

  it should "be empty if there are no comments for a commit" taggedAs RequiresDb in {
    // given
    val commitWithNoCommentsId = oid(20)

    // when
    val commentList = reactionsFinder.findReactionsForCommit(commitWithNoCommentsId)

    // then
    commentList.entireCommitReactions.comments should be(None)
  }

  it should "contain reactions for whole commit" taggedAs RequiresDb in {
    // given

    // when
    val reactionsView = reactionsFinder.findReactionsForCommit(CommitId)
    val Some(commitComments) = reactionsView.entireCommitReactions.comments

    // then
    commentMessagesWithAuthors(commitComments) should be(Set(("Monster class", "John"), ("Fix it ASAP", "Mary")))
  }

  it should "contain inline comments grouped by file and line" taggedAs RequiresDb in {
    // when
    val reactionsView = reactionsFinder.findReactionsForCommit(CommitId)

    // then
    val fileComments = reactionsView.inlineReactions

    lineReactionsFor(fileComments, "Main.scala").size should be(1)
    reactionLineNumbersFor(fileComments, "Main.scala") should be(Set(10))
    commentMessagesWithAuthorsFor(fileComments, "Main.scala", 10) should be(Set(("Cool thing", "John"), ("Indeed", "Mary")))

    lineReactionsFor(fileComments, "Database.scala").size should be(2)
    reactionLineNumbersFor(fileComments, "Database.scala") should be(Set(12, 20))
    commentMessagesWithAuthorsFor(fileComments, "Database.scala", 12) should be(Set(("Possible NPE?", "Mary"), ("Nope", "John")))
    commentMessagesWithAuthorsFor(fileComments, "Database.scala", 20) should be(Set(("Refactor that", "John")))

  }

  it should "contain inline likes grouped by file and line" taggedAs RequiresDb in {
    // when
    val reactionsView = reactionsFinder.findReactionsForCommit(CommitId)

    // then
    likesForFileAndLike(reactionsView, "Main.scala", 10).get.size should be(2)
    likesForFileAndLike(reactionsView, "Database.scala", 12).get.size should be(1)
  }

  def likesForFileAndLike(reactions: CommitReactionsView, fileName: String, lineNumber: Int) = {
    reactions.inlineReactions(fileName)(lineNumber.toString).likes
  }

  it should "have comments ordered by date starting from the oldest" taggedAs RequiresDb in {
    // given
    val baseDate = DateTime.now
    val inlineComments = List(
      CommentAssembler.commentFor(CommitId).withFileNameAndLineNumber("Exception.scala", 10).withMessage("You'd better refactor that").withAuthorId(John.id).postedAt(baseDate.plusHours(1)).get,
      CommentAssembler.commentFor(CommitId).withFileNameAndLineNumber("Exception.scala", 10).withMessage("Man, it's Monday").withAuthorId(Mary.id).postedAt(baseDate.plusHours(2)).get
    )
    inlineComments.foreach(commentDao.save)

    // when
    val commentsView = reactionsFinder.findReactionsForCommit(CommitId)

    // then
    val fileComments = commentsView.inlineReactions
    orderedCommentMessagesFor(fileComments, "Exception.scala", 10) should be(List("You'd better refactor that", "Man, it's Monday"))
  }

  it should "return author avatar, full name and id in comment" taggedAs RequiresDb in {
    // when
    val commentsView = reactionsFinder.findReactionsForCommit(CommitId)

    // then
    val Some(comments) = commentsView.entireCommitReactions.comments
    val comment = comments(0).asInstanceOf[CommentView]
    comment.authorId should equal(John.id.toString)
    comment.authorName should equal(John.name)
    comment.authorAvatarUrl should equal(John.settings.avatarUrl)
  }

  it should "return empty string as author avatar if author not registered in codebrag" taggedAs RequiresDb in {
    // given
    val dummyCommitId = ObjectIdTestUtils.oid(123123)
    val commentFromNonexistingUser = CommentAssembler.commentFor(dummyCommitId).withAuthorId(ObjectIdTestUtils.oid(1111111)).get
    commentDao.save(commentFromNonexistingUser)

    // when
    val commentsView = reactionsFinder.findReactionsForCommit(dummyCommitId)

    // then
    val Some(comments) = commentsView.entireCommitReactions.comments
    comments(0).asInstanceOf[CommentView].authorAvatarUrl should equal("")
  }

  it should "find like by id" taggedAs RequiresDb in {
    // given
    val like = LikeAssembler.likeFor(commitId).withAuthorId(user.id).get
    likeDao.save(like)

    // when
    val found = reactionsFinder.findLikeById(like.id)

    found.get.id should equal(like.id.toString)
    found.get.authorId should equal(user.id.toString)
    found.get.authorName should equal(user.name)
  }

  it should "find like by id with empty user name when like author not found" taggedAs RequiresDb in {
    // given
    val like = LikeAssembler.likeFor(commitId).withAuthorId(nonExistingAuthorId).get
    likeDao.save(like)

    // when
    val found = reactionsFinder.findLikeById(like.id)

    found.get.id should equal(like.id.toString)
    found.get.authorId should equal(nonExistingAuthorId.toString)
    found.get.authorName should be('empty)
  }

  it should "return None if like not found" taggedAs RequiresDb in {
    // given
    val nonExistingLikeId = ObjectIdTestUtils.oid(200)

    // when
    val found = reactionsFinder.findLikeById(nonExistingLikeId)

    found should be('empty)
  }

}


trait ReactionFinderVerifyHelpers {

  def lineReactionsFor(fileReactions: Map[String, Map[String, ReactionsView]], fileName: String) = {
    fileReactions(fileName)
  }

  def reactionLineNumbersFor(fileComments: Map[String, Map[String, ReactionsView]], fileName: String) = {
    lineReactionsFor(fileComments, fileName).map(_._1.toInt).toSet
  }

  def commentMessagesWithAuthorsFor(fileComments: Map[String, Map[String, ReactionsView]], fileName: String, lineNumber: Int) = {
    val Some(lineComments) = lineReactionsFor(fileComments, fileName)(lineNumber.toString).comments
    commentMessagesWithAuthors(lineComments)
  }

  def commentMessagesWithAuthors(comments: List[ReactionView]) = {
    comments.map(c => {
      val comment = c.asInstanceOf[CommentView]
      (comment.message, comment.authorName)
    }).toSet
  }

  def commentMessagesFor(comments: List[ReactionView]) = {
    comments.map(c => {
      c.asInstanceOf[CommentView].message
    }).toSet
  }

  def orderedCommentMessagesFor(fileComments: Map[String, Map[String, ReactionsView]], fileName: String, lineNumber: Int) = {
    val Some(comments) = lineReactionsFor(fileComments, fileName)(lineNumber.toString).comments
    comments.map(c => {
      c.asInstanceOf[CommentView].message
    })
  }

}

