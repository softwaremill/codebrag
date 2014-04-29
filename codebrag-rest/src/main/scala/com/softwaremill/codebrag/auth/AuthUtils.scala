package com.softwaremill.codebrag.auth

import org.scalatra.auth.Scentry
import javax.servlet.http.HttpServletRequest
import com.softwaremill.codebrag.InstanceContext

object AuthUtils {

  def cookieKey(preKey: String)(implicit request: HttpServletRequest) = {
    // to allow exists few instance on the same host but on different ports
    preKey + "_" + InstanceContext.getInstanceSettings(request.getServletContext).value
  }

  def scentryAuthKey(implicit request: HttpServletRequest) = {
    cookieKey(Scentry.scentryAuthKey)
  }

}
