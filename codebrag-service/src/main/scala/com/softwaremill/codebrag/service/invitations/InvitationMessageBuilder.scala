package com.softwaremill.codebrag.service.invitations

import com.softwaremill.codebrag.domain.User

object InvitationMessageBuilder {

  def buildMessage(user: User, url:String): String = {
   "Let's review some code!\n\n" +
   s"${user.name} invited you to join Codebrag - a simple tool that makes code review fun and well-organized.\n\n" +
   s"Use this link to register:\n$url"
  }

  def buildSubject(userName:String):String = {
    s"You have been invited to use codebrag by $userName";
  }

}
