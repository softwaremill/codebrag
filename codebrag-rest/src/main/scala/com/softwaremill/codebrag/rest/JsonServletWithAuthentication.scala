package com.softwaremill.codebrag.rest

import org.json4s.{ DefaultFormats, Formats }
import com.softwaremill.codebrag.auth.RememberMeSupport

abstract class JsonServletWithAuthentication extends JsonServlet with RememberMeSupport {

}