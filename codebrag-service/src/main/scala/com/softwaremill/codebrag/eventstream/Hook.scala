package com.softwaremill.codebrag.eventstream

import com.softwaremill.codebrag.common.Clock
import com.softwaremill.codebrag.domain._
import org.joda.time.DateTime

sealed trait Hook {
  val hookDate: DateTime
  def hookName: String
}

case class LikeHook(
   commitInfo: Option[CommitInfo],
   user: Option[User],
   like: Like,
   override val hookName: String
  )(implicit clock: Clock)
  extends Hook {

  override val hookDate: DateTime = clock.nowUtc
}

case class UnlikeHook(
  commitInfo: Option[CommitInfo],
  user: Option[User],
  like: Like,
  override val hookName: String
  ) (implicit clock: Clock)
  extends Hook {

  override val hookDate: DateTime = clock.nowUtc
}

case class CommentAddedHook(
  commitInfo: Option[CommitInfo],
  user: Option[User],
  comment: Comment,
  override val hookName: String
  ) (implicit clock: Clock)
  extends Hook {

  override val hookDate: DateTime = clock.nowUtc
}

case class CommitReviewedHook(
  commitInfo: CommitInfo,
  user: Option[User],
  override val hookName: String
  ) (implicit clock: Clock)
  extends Hook {

  override val hookDate: DateTime = clock.nowUtc
}

case class NewCommitsLoadedHook(
  user: Option[User],
  repoName: String,
  currentSHA: String,
  newCommits: List[PartialCommitInfo],
  override val hookName: String
  ) (implicit clock: Clock)
  extends Hook {

  override val hookDate: DateTime = clock.nowUtc
}

case class NewUserRegisteredHook(
  user: Option[User],
  login: String,
  fullName: String,
  override val hookName: String
  ) (implicit clock: Clock)
  extends Hook {

  override val hookDate: DateTime = clock.nowUtc
}
