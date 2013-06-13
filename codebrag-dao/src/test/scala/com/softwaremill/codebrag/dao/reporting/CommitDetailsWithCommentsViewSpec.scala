package com.softwaremill.codebrag.dao.reporting

import org.scalatest.matchers.ShouldMatchers
import org.scalatest.{FlatSpec, BeforeAndAfterEach}
import org.joda.time.DateTime
import com.softwaremill.codebrag.dao.reporting.views._
import com.softwaremill.codebrag.domain.{FileDiffStats, DiffLine, CommitFileDiff}
import com.softwaremill.codebrag.dao.reporting.views.CommentsView
import com.softwaremill.codebrag.dao.reporting.views.SingleCommentView

class CommitDetailsWithCommentsViewSpec extends FlatSpec with BeforeAndAfterEach with ShouldMatchers with CommentListFinderVerifyHelpers {

  val Commit = CommitView("123", "123abc", "This is commit message", "John Doe", DateTime.now.toDate)
  val Lines = List(DiffLine("line one", 1, 2, "added"), DiffLine("line two", 2, 2, "added"))
  val Diffs = List(CommitFileDiff("test.txt", "added", Lines, FileDiffStats(2, 0)))
  val EmptyComments = CommentsView(comments = Nil, inlineComments = Map())

  it should "have empty comments list when commit has no comments" in {
    // given
    val comments = EmptyComments

    // when
    val commitWithComments = CommitDetailsWithCommentsView.buildFrom(Commit, comments, Diffs)

    // then
    commitWithComments.reactions.comments should be('empty)
    val lineComments = commitWithComments.lineReactions should be('empty)
  }

  it should "have comments when commit has some general comments" in {
    // given
    val generalComment = SingleCommentView("123", "Mary Smith", "Comment for commit", DateTime.now.toDate)
    val comments = CommentsView(comments = List(generalComment), inlineComments = Map())

    // when
    val commitWithComments = CommitDetailsWithCommentsView.buildFrom(Commit, comments, Diffs)

    // then
    commitWithComments.reactions.comments.head should be(generalComment)
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
    val fileLines = commitWithComments.lineReactions("test.txt")
    // keys need to be strings in order to serialize to JSON
    fileLines("0") should equal(List(lineCommentOne))
    fileLines("1") should equal(List(lineCommentTwo))
  }

  it should "have diff stats for file" in {
    // when
    val commitWithComments = CommitDetailsWithCommentsView.buildFrom(Commit, EmptyComments, Diffs)

    // then
    commitWithComments.diff(0).diffStats.added should be(2)
    commitWithComments.diff(0).diffStats.removed should be(0)
  }

  it should "supress diffs for large files" in {
    val largeFileSize = CommitDetailsWithCommentsView.MaxAcceptableDiffLinesCount + 100; // diff size exceeding max acceptable size
    val diffLines = buildDummyDiffWithLinesNumber(largeFileSize)
    val largeFileDiff = CommitFileDiff("largefile.txt", "added", diffLines, FileDiffStats(largeFileSize, 0))

    val viewWithSupressedDiff = CommitDetailsWithCommentsView.buildFrom(Commit, EmptyComments, List(largeFileDiff))

    viewWithSupressedDiff.diff should be('empty)
    viewWithSupressedDiff.supressedFiles.size should be(1)
  }


  it should "keep regular diff for small files" in {
    val smallFileSize = CommitDetailsWithCommentsView.MaxAcceptableDiffLinesCount - 100; // diff size smaller than max acceptable size
    val diffLines = buildDummyDiffWithLinesNumber(smallFileSize)
    val smallFileDiff = CommitFileDiff("largefile.txt", "added", diffLines, FileDiffStats(smallFileSize, 0))

    val viewWithRegularDiff = CommitDetailsWithCommentsView.buildFrom(Commit, EmptyComments, List(smallFileDiff))

    viewWithRegularDiff.diff.size should be(1)
    viewWithRegularDiff.supressedFiles should be('empty)
  }

  private def buildDummyDiffWithLinesNumber(largeFileSize: Int): List[DiffLine] = {
    (1 to largeFileSize).map(i => {
      DiffLine(s"line content$i", i, i, "added")
    }).toList
  }

}
