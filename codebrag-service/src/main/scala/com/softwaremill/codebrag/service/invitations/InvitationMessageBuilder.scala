package com.softwaremill.codebrag.service.invitations

import com.softwaremill.codebrag.domain.User

object InvitationMessageBuilder {

  def buildMessage(user: User, url:String): String = {
   "Let's review some code!\n\n" +
   s"${user.name} invited you to join Codebrag - a super simple code review tool.\n\n" +
   s"Use this link to register:\n$url"
  }

  def buildSubject(userName:String):String = {
    s"You have been invited to use codebrag by $userName";
  }

}
