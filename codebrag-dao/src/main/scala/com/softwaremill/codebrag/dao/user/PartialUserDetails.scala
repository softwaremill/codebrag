package com.softwaremill.codebrag.dao.user

import com.softwaremill.codebrag.domain.UserLike

case class PartialUserDetails(name: String, email: String, avatarUrl: String)

object PartialUserDetails extends ((String, String, String) => PartialUserDetails) {
  implicit object UserLikePartialUserDetails extends UserLike[PartialUserDetails] {
    def userFullName(userLike: PartialUserDetails) = userLike.name
    def userEmail(userLike: PartialUserDetails) = userLike.email
  }
}
