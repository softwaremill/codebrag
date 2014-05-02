package com.softwaremill.codebrag.web

import org.json4s.{ext, DefaultFormats}
import com.softwaremill.codebrag.dao.finders.views.CommitState
import com.softwaremill.codebrag.licence.LicenceType

object CodebragSpecificJSONFormats extends DefaultFormats {
  private val types = List(CommitState, LicenceType)

  val all = types.map(new ext.EnumNameSerializer(_))
}
