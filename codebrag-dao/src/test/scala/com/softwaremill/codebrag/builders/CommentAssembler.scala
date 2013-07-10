package com.softwaremill.codebrag.builders

import org.joda.time.DateTime
import org.bson.types.ObjectId
import com.softwaremill.codebrag.domain.{Like, Comment}

object CommentAssembler {

  def commentFor(commitId: ObjectId) = {
    val dummyComment = Comment(new ObjectId, new ObjectId, new ObjectId, DateTime.now, "Comment message")
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

object LikeAssembler {

  private val dummyLike = Like(new ObjectId, new ObjectId, new ObjectId, DateTime.now)

  def likeFor(commitId: ObjectId) = {
    new Assembler(dummyLike.copy(commitId = commitId))
  }

  class Assembler(var base: Like) {

    def withFileNameAndLineNumber(newFileName: String, newLineNumber: Int) = {
      base = base.copy(fileName = Some(newFileName), lineNumber = Some(newLineNumber))
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