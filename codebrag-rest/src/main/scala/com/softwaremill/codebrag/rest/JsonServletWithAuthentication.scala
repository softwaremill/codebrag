package pl.softwaremill.codebrag.rest

import org.json4s.{ DefaultFormats, Formats }
import pl.softwaremill.codebrag.auth.RememberMeSupport

abstract class JsonServletWithAuthentication extends JsonServlet with RememberMeSupport {

}