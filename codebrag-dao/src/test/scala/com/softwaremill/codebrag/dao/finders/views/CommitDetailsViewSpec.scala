package com.softwaremill.codebrag.dao.finders.views

import org.scalatest.matchers.ShouldMatchers
import org.scalatest.{FlatSpec, BeforeAndAfterEach}
import org.joda.time.DateTime
import com.softwaremill.codebrag.dao.finders.reaction.ReactionFinderVerifyHelpers
import com.softwaremill.codebrag.domain.DiffLine
import com.softwaremill.codebrag.domain.CommitFileDiff
import com.softwaremill.codebrag.domain.FileDiffStats

class CommitDetailsViewSpec extends FlatSpec with BeforeAndAfterEach with ShouldMatchers with ReactionFinderVerifyHelpers {

  val Commit = CommitView("123", "codebrag", "123abc", "This is commit message", "John Doe", "john@example.org", DateTime.now.toDate)
  val Lines = List(DiffLine("line one", 1, 2, "added"), DiffLine("line two", 2, 2, "added"))
  val Diffs = List(CommitFileDiff("test.txt", "added", Lines, FileDiffStats(2, 0)))
  val EmptyReactions = CommitReactionsView(ReactionsView(None, None), Map())

  it should "have empty comments list when commit has no comments" in {
    // given
    val reactions = EmptyReactions

    // when
    val commitWithNoReactions = CommitDetailsView.buildFrom(Commit, reactions, Diffs)

    // then
    commitWithNoReactions.reactions.comments should be(None)
    commitWithNoReactions.lineReactions should be('empty)
  }

  it should "have comments when commit has some general comments" in {
    // given
    val generalComments = List(CommentView("123", "Mary Smith", "1", "Comment for commit", DateTime.now.toDate, ""))
    val reactions = CommitReactionsView(ReactionsView(Some(generalComments), None), Map())

    // when
    val commitWithReactions = CommitDetailsView.buildFrom(Commit, reactions, Diffs)

    // then
    val Some(comments) = commitWithReactions.reactions.comments
    comments should be(generalComments)
  }

  it should "have inline comments when commit has some lines commented" in {
    // given
    val lineCommentOne = CommentView("123", "John Doe", "1", "Line comment one", DateTime.now.toDate, "")
    val lineCommentTwo = CommentView("456", "Mary Smith", "2", "Line comment two", DateTime.now.toDate, "")
    val reactionsForLineZero = ReactionsView(Some(List(lineCommentOne)), None)
    val reactionsForLineOne = ReactionsView(Some(List(lineCommentTwo)), None)
    val fileComments = Map("test.txt" -> Map(0.toString -> reactionsForLineZero, 1.toString -> reactionsForLineOne))
    val comments = CommitReactionsView(ReactionsView(None, None), fileComments)

    // when
    val commitWithReactions = CommitDetailsView.buildFrom(Commit, comments, Diffs)

    // then
    val fileReactions = commitWithReactions.lineReactions("test.txt")
    // keys need to be strings in order to serialize to JSON
    fileReactions("0") should equal(reactionsForLineZero)
    fileReactions("1") should equal(reactionsForLineOne)
  }

  it should "have diff stats for file" in {
    // when
    val commitWithComments = CommitDetailsView.buildFrom(Commit, EmptyReactions, Diffs)

    // then
    commitWithComments.diff(0).diffStats.added should be(2)
    commitWithComments.diff(0).diffStats.removed should be(0)
  }

  it should "supress diffs for large files" in {
    val largeFileSize = CommitDetailsView.MaxAcceptableDiffLinesCount + 100; // diff size exceeding max acceptable size
    val diffLines = buildDummyDiffWithLinesNumber(largeFileSize)
    val largeFileDiff = CommitFileDiff("largefile.txt", "added", diffLines, FileDiffStats(largeFileSize, 0))

    val viewWithSupressedDiff = CommitDetailsView.buildFrom(Commit, EmptyReactions, List(largeFileDiff))

    viewWithSupressedDiff.diff should be('empty)
    viewWithSupressedDiff.supressedFiles.size should be(1)
  }


  it should "keep regular diff for small files" in {
    val smallFileSize = CommitDetailsView.MaxAcceptableDiffLinesCount - 100; // diff size smaller than max acceptable size
    val diffLines = buildDummyDiffWithLinesNumber(smallFileSize)
    val smallFileDiff = CommitFileDiff("largefile.txt", "added", diffLines, FileDiffStats(smallFileSize, 0))

    val viewWithRegularDiff = CommitDetailsView.buildFrom(Commit, EmptyReactions, List(smallFileDiff))

    viewWithRegularDiff.diff.size should be(1)
    viewWithRegularDiff.supressedFiles should be('empty)
  }

  private def buildDummyDiffWithLinesNumber(largeFileSize: Int): List[DiffLine] = {
    (1 to largeFileSize).map(i => {
      DiffLine(s"line content$i", i, i, "added")
    }).toList
  }

}
