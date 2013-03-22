package com.softwaremill.codebrag.rest

import com.softwaremill.codebrag.auth.RememberMeSupport
import org.json4s.{ DefaultFormats, Formats }

abstract class JsonServletWithAuthentication extends JsonServlet with RememberMeSupport {
  import com.softwaremill.codebrag.service.user.Authenticator

}