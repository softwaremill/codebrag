package pl.softwaremill.codebrag.rest

import pl.softwaremill.codebrag.common.{ UptimeSupport, JsonWrapper }

class UptimeServlet extends JsonServlet with UptimeSupport {

  get("/") {
    JsonWrapper(serverUptime())
  }

}
