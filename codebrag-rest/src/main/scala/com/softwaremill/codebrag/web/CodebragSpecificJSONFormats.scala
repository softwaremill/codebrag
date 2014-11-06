package com.softwaremill.codebrag.web

import org.json4s._
import com.softwaremill.codebrag.dao.finders.views.CommitState
import org.bson.types.ObjectId
import org.json4s.JsonAST.JString

object CodebragSpecificJSONFormats extends DefaultFormats {
  private val types = List(CommitState)

  val all = types.map(new ext.EnumNameSerializer(_)).toList

  object SimpleObjectIdSerializer extends CustomSerializer[ObjectId] (format =>
    ( {
        case JString(oid) => new ObjectId(oid)
        case JNull => null
      },
      {
        case oid: ObjectId => JString(oid.toString)
      }
    )
  )

}
