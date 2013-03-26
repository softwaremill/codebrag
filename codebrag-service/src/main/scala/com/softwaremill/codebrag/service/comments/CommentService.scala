package com.softwaremill.codebrag.service.comments

import com.softwaremill.codebrag.dao.{UserDAO, CommitInfoDAO}
import com.softwaremill.codebrag.domain.{User, CommitComment, CommitInfo}
import pl.softwaremill.common.util.time.Clock
import com.softwaremill.codebrag.common.IdGenerator

class CommentService(commitDAO: CommitInfoDAO, userDAO: UserDAO)(implicit idGenerator: IdGenerator, clock: Clock) {

  def addCommentToCommit(command: AddCommentCommand) = {

    val commitId: String = command.commitId
    val commitOpt: Option[CommitInfo] = commitDAO.findBySha(commitId)

    commitOpt match {
      case Some(commit) => {
        val id = idGenerator.generateRandom
        val time = clock.currentDateTimeUTC()
        val user = userDAO.findByLoginOrEmail(command.authorLogin).get
        val newComment = CommitComment(id, user.name, command.message, time)
        val updatedCommit = commit.addComment(newComment)
        commitDAO.storeCommit(updatedCommit)
        id
      }
      case None => throw new IllegalArgumentException(s"Cannot load commit with id = $commitId")
    }
  }
}

case class AddCommentCommand(commitId: String, authorLogin: String, message: String)
