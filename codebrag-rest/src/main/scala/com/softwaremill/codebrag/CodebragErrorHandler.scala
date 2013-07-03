package com.softwaremill.codebrag.rest

import org.scalatra.ScalatraBase

trait CodebragErrorHandler extends ScalatraBase {

  notFound {
    redirect("/#/notfound")
  }

  error {
    case e => redirect("/#/error")
  }

}
