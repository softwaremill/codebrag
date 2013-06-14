package com.softwaremill.codebrag.builders

import org.joda.time.DateTime
import org.bson.types.ObjectId
import com.softwaremill.codebrag.domain.{UserComment, InlineCommitComment, EntireCommitComment}

object CommentAssembler {

  val dummyComment = UserComment(new ObjectId, new ObjectId, new ObjectId, DateTime.now, "Inline comment message")

  def commentFor(commitId: ObjectId) = {
    new UserCommentAssembler(dummyComment.copy(commitId = commitId))
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

}
