package com.softwaremill.codebrag.rest

import com.softwaremill.codebrag.common.{ UptimeSupport, JsonWrapper }

class UptimeServlet extends JsonServlet with UptimeSupport {

  get("/") {
    JsonWrapper(serverUptime())
  }

}
