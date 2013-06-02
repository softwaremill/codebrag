package com.softwaremill.codebrag.dao.reporting

import org.scalatest.matchers.ShouldMatchers
import org.scalatest.{FlatSpec, BeforeAndAfterEach}
import org.joda.time.DateTime
import com.softwaremill.codebrag.dao.reporting.views._
import com.softwaremill.codebrag.domain.DiffLine
import com.softwaremill.codebrag.dao.reporting.views.CommentsView
import com.softwaremill.codebrag.domain.CommitFileDiff
import com.softwaremill.codebrag.dao.reporting.views.SingleCommentView

class CommitDetailsWithCommentsViewSpec extends FlatSpec with BeforeAndAfterEach with ShouldMatchers with CommentListFinderVerifyHelpers {

  val Commit = CommitView("123", "123abc", "This is commit message", "John Doe", "John Doe", DateTime.now.toDate)
  val Lines = List(DiffLine("line one", 1, 2, "added"), DiffLine("line two", 2, 2, "added"))
  val Diffs = List(CommitFileDiff("test.txt", "added", Lines))

  it should "have empty comments list when commit has no comments" in {
    // given
    val comments = CommentsView(comments = Nil, inlineComments = Map())

    // when
    val commitWithComments = CommitDetailsWithCommentsView.buildFrom(Commit, comments, Diffs)

    // then
    commitWithComments.comments should be('empty)
    val lineComments = commitWithComments.inlineComments should be('empty)
  }

  it should "have comments when commit has some general comments" in {
    // given
    val generalComment = SingleCommentView("123", "Mary Smith", "Comment for commit", DateTime.now.toDate)
    val comments = CommentsView(comments = List(generalComment), inlineComments = Map())

    // when
    val commitWithComments = CommitDetailsWithCommentsView.buildFrom(Commit, comments, Diffs)

    // then
    commitWithComments.comments.head should be(generalComment)
  }

  it should "have inline comments when commit has some lines commented" in {
    // given
    val lineCommentOne = SingleCommentView("123", "John Doe", "Line comment one", DateTime.now.toDate)
    val lineCommentTwo = SingleCommentView("456", "Mary Smith", "Line comment two", DateTime.now.toDate)
    val fileComments = Map("test.txt" -> Map(0 -> List(lineCommentOne), 1 -> List(lineCommentTwo)))
    val comments = CommentsView(comments = Nil, inlineComments = fileComments)

    // when
    val commitWithComments = CommitDetailsWithCommentsView.buildFrom(Commit, comments, Diffs)

    // then
    val fileLines = commitWithComments.inlineComments("test.txt")
    // keys need to be strings in order to serialize to JSON
    fileLines("0") should equal(List(lineCommentOne))
    fileLines("1") should equal(List(lineCommentTwo))
  }

}
