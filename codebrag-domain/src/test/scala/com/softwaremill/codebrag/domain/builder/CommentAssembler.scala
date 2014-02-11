package com.softwaremill.codebrag.domain.builder

import org.joda.time.DateTime
import org.bson.types.ObjectId
import com.softwaremill.codebrag.domain.{Like, Comment}
import com.softwaremill.codebrag.common.RealTimeClock

object CommentAssembler {

  def commentFor(commitId: ObjectId) = {
    new Assembler(Comment(new ObjectId, commitId, new ObjectId, RealTimeClock.currentDateTimeUTC, "Comment message"))
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


  def likeFor(commitId: ObjectId) = {
    new Assembler(Like(new ObjectId, commitId, new ObjectId, DateTime.now))
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