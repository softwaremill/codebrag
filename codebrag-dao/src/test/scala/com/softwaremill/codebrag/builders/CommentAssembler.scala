package com.softwaremill.codebrag.builders

import org.joda.time.DateTime
import org.bson.types.ObjectId
import com.softwaremill.codebrag.domain.{InlineCommitComment, EntireCommitComment}

object CommentAssembler {

  private val entireCommitCommentBase = new EntireCommitComment(new ObjectId, new ObjectId, new ObjectId, "Comment message", DateTime.now)
  private val inlineCommitCommentBase = InlineCommitComment(entireCommitCommentBase, "file_1.txt", 1)

  def commitCommentFor(commitId: ObjectId) = {
    new EntireCommitCommentAssembler(entireCommitCommentBase.copy(commitId = commitId))
  }

  def inlineCommentFor(commitId: ObjectId) = {
    val base = entireCommitCommentBase.copy(commitId = commitId)
    new InlineCommitCommentAssembler(inlineCommitCommentBase.copy(commitComment = base))
  }

  class EntireCommitCommentAssembler(var base: EntireCommitComment) {

    def withMessage(newMessage: String) = {
      base = base.copy(message = newMessage)
      this
    }


    def withId(newId: ObjectId) = {
      base = base.copy(id = newId)
      this
    }

    def withCommitId(newId: ObjectId) = {
      base = base.copy(id = newId)
      this
    }

    def withDate(newDate: DateTime) = {
      base = base.copy(postingTime = newDate)
      this
    }

    def withAuthorId(newAuthorId: ObjectId) = {
      base = base.copy(authorId = newAuthorId)
      this
    }

    def postedAt(newDate: DateTime) = {
      base = base.copy(postingTime = newDate)
      this
    }

    def get = base

  }

  class InlineCommitCommentAssembler(var base: InlineCommitComment) {

    def withFileNameAndLineNumber(newFileName: String, newLineNumber: Int) = {
      base = base.copy(fileName = newFileName, lineNumber = newLineNumber)
      this
    }

    def withMessage(newMessage: String) = {
      base = base.copy(commitComment = base.commitComment.copy(message = newMessage))
      this
    }

    def withAuthorId(newAuthorId: ObjectId) = {
      base = base.copy(commitComment = base.commitComment.copy(authorId = newAuthorId))
      this
    }

    def postedAt(newDate: DateTime) = {
      base = base.copy(commitComment = base.commitComment.copy(postingTime = newDate))
      this
    }

    def get = base

  }

}
