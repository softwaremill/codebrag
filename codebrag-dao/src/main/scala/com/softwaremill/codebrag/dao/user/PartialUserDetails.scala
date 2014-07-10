package com.softwaremill.codebrag.dao.user

import com.softwaremill.codebrag.domain.UserLike
import org.bson.types.ObjectId

case class PartialUserDetails(id: ObjectId, name: String, email: String, avatarUrl: String)

object PartialUserDetails extends ((ObjectId, String, String, String) => PartialUserDetails) {
  implicit object UserLikePartialUserDetails extends UserLike[PartialUserDetails] {
    def userFullName(userLike: PartialUserDetails) = userLike.name
    def userEmails(userLike: PartialUserDetails) = Set(userLike.email)
  }
}
