package com.softwaremill.codebrag.dao.reporting

import org.scalatest.matchers.ShouldMatchers
import org.scalatest.{FlatSpec, BeforeAndAfterEach}
import org.joda.time.DateTime
import com.softwaremill.codebrag.domain.{DiffLine, CommitFileDiff}

class CommitDetailsWithCommentsSpec extends FlatSpec with BeforeAndAfterEach with ShouldMatchers with CommentListFinderVerifyHelpers {

  val Commit = CommitListItemDTO("123", "123abc", "This is commit message", "John Doe", "John Doe", DateTime.now.toDate)
  val Lines = List(DiffLine("line one", 1, 2, "added"), DiffLine("line two", 2, 2, "added"))
  val Diffs = List(CommitFileDiff("test.txt", "added", Lines))

  it should "have empty comments list when commit has no comments" in {
    // given
    val comments = CommentsView(comments = Nil, inlineComments = Nil)

    // when
    val commitWithComments = CommitDetailsWithComments.buildFrom(Commit, comments, Diffs)

    // then
    commitWithComments.comments should be('empty)
    val lineComments = commitWithComments.files.head.lines.map(_.comments)
    lineComments.foreach(_ should be('empty))
  }

  it should "have comments when commit has some general comments" in {
    // given
    val generalComment = SingleCommentView("123", "Mary Smith", "Comment for commit", DateTime.now.toDate)
    val comments = CommentsView(comments = List(generalComment), inlineComments = Nil)

    // when
    val commitWithComments = CommitDetailsWithComments.buildFrom(Commit, comments, Diffs)

    // then
    commitWithComments.comments.head should be(generalComment)
  }

  it should "have inline comments when commit has some lines commented" in {
    // given
    val lineCommentOne = SingleCommentView("123", "John Doe", "Line comment one", DateTime.now.toDate)
    val lineCommentTwo = SingleCommentView("456", "Mary Smith", "Line comment two", DateTime.now.toDate)
    val fileComments = FileCommentsView("test.txt", Map(0 -> List(lineCommentOne), 1 -> List(lineCommentTwo)))
    val comments = CommentsView(comments = Nil, inlineComments = List(fileComments))

    // when
    val commitWithComments = CommitDetailsWithComments.buildFrom(Commit, comments, Diffs)

    // then
    val fileLines = commitWithComments.files.find(_.filename == "test.txt").get.lines
    fileLines.find(_.line == "line one").get.comments should equal(List(lineCommentOne))
    fileLines.find(_.line == "line two").get.comments should equal(List(lineCommentTwo))
  }

}
