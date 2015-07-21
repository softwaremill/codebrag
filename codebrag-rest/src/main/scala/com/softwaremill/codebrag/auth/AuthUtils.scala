package com.softwaremill.codebrag.auth

import javax.servlet.http.HttpServletRequest

import org.scalatra.auth.Scentry

object AuthUtils {

  def cookieKey(preKey: String)(implicit request: HttpServletRequest) = {
    // to allow exists few instance on the same host but on different ports
    preKey// + "_" + InstanceContext.getInstanceSettings(request.getServletContext).value
  }

  def scentryAuthKey(implicit request: HttpServletRequest) = {
    cookieKey(Scentry.scentryAuthKey)
  }

}
