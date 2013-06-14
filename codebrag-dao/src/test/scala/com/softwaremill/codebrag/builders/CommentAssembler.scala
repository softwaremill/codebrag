package com.softwaremill.codebrag.builders

import org.joda.time.DateTime
import org.bson.types.ObjectId
import com.softwaremill.codebrag.domain.Comment

object CommentAssembler {

  private val dummyComment = Comment(new ObjectId, new ObjectId, new ObjectId, DateTime.now, "Comment message")

  def commentFor(commitId: ObjectId) = {
    new Assembler(dummyComment.copy(commitId = commitId))
  }

  class Assembler(var base: Comment) {

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
