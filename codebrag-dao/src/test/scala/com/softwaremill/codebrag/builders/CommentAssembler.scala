package com.softwaremill.codebrag.builders

import org.joda.time.DateTime
import org.bson.types.ObjectId
import com.softwaremill.codebrag.domain.{UserComment, InlineCommitComment, EntireCommitComment}

object CommentAssembler {

  private val entireCommitCommentBase = new EntireCommitComment(new ObjectId, new ObjectId, new ObjectId, "Comment message", DateTime.now)
  private val inlineCommitCommentBase = InlineCommitComment(new ObjectId, new ObjectId, new ObjectId, "Inline comment message", DateTime.now, "file_1.txt", 1)

  val userComment = UserComment(new ObjectId, new ObjectId, new ObjectId, DateTime.now, "Inline comment message")

  def userCommentForCommit(commitId: ObjectId) = new UserCommentAssembler(userComment)

  def commitCommentFor(commitId: ObjectId) = {
    new EntireCommitCommentAssembler(entireCommitCommentBase.copy(commitId = commitId))
  }

  def inlineCommentFor(commitId: ObjectId) = {
    new InlineCommitCommentAssembler(inlineCommitCommentBase.copy(commitId = commitId))
  }

  class UserCommentAssembler(var base: UserComment) {

    def withFileNameAndLineNumber(newFileName: String, newLineNumber: Int) = {
      base = base.copy(fileName = Some(newFileName), lineNumber = Some(newLineNumber))
      this
    }

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

}
