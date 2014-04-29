package com.softwaremill.codebrag

import javax.servlet.ServletContext

object InstanceContext {

  def get(context: ServletContext): Beans = {
    context.getAttribute("codebrag").asInstanceOf[Beans]
  }

  def put(context: ServletContext, beans: Beans) {
    context.setAttribute("codebrag", beans)
  }

  def getInstanceSettings(context: ServletContext) = {
    get(context).InstanceId
  }

}