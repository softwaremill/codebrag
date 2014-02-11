package com.softwaremill.codebrag.dao.reporting

import com.softwaremill.codebrag.dao.ObjectIdTestUtils._
import com.softwaremill.codebrag.dao._
import org.scalatest.matchers.ShouldMatchers
import com.softwaremill.codebrag.domain.{Authentication, User}
import org.joda.time.DateTime
import com.softwaremill.codebrag.dao.reporting.views.{CommitReactionsView, ReactionsView, CommentView, ReactionView}
import com.softwaremill.codebrag.domain.builder.{LikeAssembler, CommentAssembler}
import com.softwaremill.codebrag.dao.user.MongoUserDAO
import com.softwaremill.codebrag.test.{FlatSpecWithMongo, ClearMongoDataAfterTest}
import com.softwaremill.codebrag.dao.reaction.{MongoLikeDAO, MongoCommitCommentDAO}

class MongoReactionFinderSpec extends FlatSpecWithMongo with ClearMongoDataAfterTest with ShouldMatchers with ReactionFinderVerifyHelpers {

  val userDao = new MongoUserDAO
  val commentDao = new MongoCommitCommentDAO
  val likeDao = new MongoLikeDAO
  var reactionsFinder: MongoReactionFinder = _

  val CommitId = oid(1)

  val John = User(oid(2), Authentication.basic("john", "pass"), "John", "john@doe.com", "123abc", "http://john.doe.com/avatar")
  val Mary = User(oid(3), Authentication.basic("mary", "pass"), "Mary", "mary@smith.com", "123abc", "http://mary.com/avatar")

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
    reactionsFinder = new MongoReactionFinder

    StoredCommitComments.foreach(commentDao.save)
    StoredInlineComments.foreach(commentDao.save)

    StoredInlineLikes.foreach(likeDao.save)

    List(John, Mary).foreach(userDao.add)
  }

  it should "be empty if there are no comments for a commit" taggedAs (RequiresDb) in {
    // given
    val commitWithNoCommentsId = oid(20)

    // when
    val commentList = reactionsFinder.findReactionsForCommit(commitWithNoCommentsId)

    // then
    commentList.entireCommitReactions.comments should be(None)
  }

  it should "contain reactions for whole commit" taggedAs (RequiresDb) in {
    // given

    // when
    val reactionsView = reactionsFinder.findReactionsForCommit(CommitId)
    val Some(commitComments) = reactionsView.entireCommitReactions.comments

    // then
    commentMessagesWithAuthors(commitComments) should be(Set(("Monster class", "John"), ("Fix it ASAP", "Mary")))
  }

  it should "contain inline comments grouped by file and line" taggedAs (RequiresDb) in {
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

  it should "contain inline likes grouped by file and line" taggedAs (RequiresDb) in {
    // when
    val reactionsView = reactionsFinder.findReactionsForCommit(CommitId)

    // then
    likesForFileAndLike(reactionsView, "Main.scala", 10).get.size should be(2)
    likesForFileAndLike(reactionsView, "Database.scala", 12).get.size should be(1)
  }

  def likesForFileAndLike(reactions: CommitReactionsView, fileName: String, lineNumber: Int) = {
    reactions.inlineReactions(fileName)(lineNumber.toString).likes
  }

  it should "have comments ordered by date starting from the oldest" taggedAs (RequiresDb) in {
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

  it should "return author avatar, full name and id in comment" in {
    // when
    val commentsView = reactionsFinder.findReactionsForCommit(CommitId)

    // then
    val Some(comments) = commentsView.entireCommitReactions.comments
    val comment = comments(0).asInstanceOf[CommentView]
    comment.authorId should equal(John.id.toString)
    comment.authorName should equal(John.name)
    comment.authorAvatarUrl should equal(Some(John.settings.avatarUrl))
  }

  it should "return empty string as author avatar if author not registered in codebrag" in {
    // given
    val dummyCommitId = ObjectIdTestUtils.oid(123123)
    val commentFromNonexistingUser = CommentAssembler.commentFor(dummyCommitId).withAuthorId(ObjectIdTestUtils.oid(1111111)).get
    commentDao.save(commentFromNonexistingUser)

    // when
    val commentsView = reactionsFinder.findReactionsForCommit(dummyCommitId)

    // then
    val Some(comments) = commentsView.entireCommitReactions.comments
    comments(0).asInstanceOf[CommentView].authorAvatarUrl should equal(None)
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

