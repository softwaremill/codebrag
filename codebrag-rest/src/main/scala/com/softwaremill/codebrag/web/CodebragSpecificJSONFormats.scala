package com.softwaremill.codebrag.web

import org.json4s.{ext, DefaultFormats}
import com.softwaremill.codebrag.dao.finders.views.CommitState

object CodebragSpecificJSONFormats extends DefaultFormats {
  private val types = List(CommitState)

  val all = types.map(new ext.EnumNameSerializer(_))
}
