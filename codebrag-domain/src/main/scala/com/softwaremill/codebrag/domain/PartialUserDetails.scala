package com.softwaremill.codebrag.domain

import org.bson.types.ObjectId

case class PartialUserDetails(id: ObjectId, name: String, email: String, avatarUrl: String, aliases: UserAliases)

object PartialUserDetails extends ((ObjectId, String, String, String, UserAliases) => PartialUserDetails) {
  implicit object UserLikePartialUserDetails extends UserLike[PartialUserDetails] {
    def userFullName(userLike: PartialUserDetails) = userLike.name
    def userEmails(userLike: PartialUserDetails) = Set(userLike.email) ++ userLike.aliases.emailAliases.map(_.alias)
  }
}
