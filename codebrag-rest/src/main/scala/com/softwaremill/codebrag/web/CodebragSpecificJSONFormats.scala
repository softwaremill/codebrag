package com.softwaremill.codebrag.web

import org.json4s.{ext, DefaultFormats}
import com.softwaremill.codebrag.dao.finders.views.CommitReviewState

object CodebragSpecificJSONFormats extends DefaultFormats {
  private val types = List(CommitReviewState)

  val all = types.map(new ext.EnumNameSerializer(_))
}
