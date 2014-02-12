package com.softwaremill.codebrag.dao.finders.views

case class NotificationCountersView(pendingCommitCount: Long, followupCount: Long) {
  def nonEmpty = pendingCommitCount > 0 || followupCount > 0
}
