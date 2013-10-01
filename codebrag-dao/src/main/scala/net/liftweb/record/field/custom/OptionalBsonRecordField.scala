package net.liftweb.record.field.custom


import net.liftweb.mongodb.record.{BsonMetaRecord, BsonRecord}
import net.liftweb.record.{OptionalTypedField, Field}
import net.liftweb.common.{Full, Empty, Box}
import net.liftweb.json.JsonAST._
import net.liftweb.http.js.JE.JsNull
import net.liftweb.http.js.JsExp
import net.liftweb.json.Printer
import xml.NodeSeq
import com.mongodb.DBObject

/**
 * Based on [[net.liftweb.mongodb.record.field.BsonRecordField]].
 */
class OptionalBsonRecordField[OwnerType <: BsonRecord[OwnerType], SubRecordType <: BsonRecord[SubRecordType]]
(rec: OwnerType, valueMeta: BsonMetaRecord[SubRecordType])(implicit subRecordType: Manifest[SubRecordType])
  extends Field[SubRecordType, OwnerType]
  with OptionalTypedField[SubRecordType]
{
  def this(rec: OwnerType, valueMeta: BsonMetaRecord[SubRecordType], value: SubRecordType)
          (implicit subRecordType: Manifest[SubRecordType]) = {
    this(rec, value.meta)
    set(Some(value))
  }

  def this(rec: OwnerType, valueMeta: BsonMetaRecord[SubRecordType], value: Box[SubRecordType])
          (implicit subRecordType: Manifest[SubRecordType]) = {
    this(rec, valueMeta)
    setBox(value)
  }

  def owner = rec
  def asJs = asJValue match {
    case JNothing => JsNull
    case jv => new JsExp {
      lazy val toJsCmd = Printer.compact(render(jv))
    }
  }
  def toForm: Box[NodeSeq] = Empty
  def defaultValue = valueMeta.createRecord

  def setFromString(s: String): Box[SubRecordType] = valueMeta.fromJsonString(s)

  def setFromAny(in: Any): Box[SubRecordType] = in match {
    case dbo: DBObject => setBox(Full(valueMeta.fromDBObject(dbo)))
    case _ => genericSetFromAny(in)
  }

  def asJValue: JValue = valueBox.map(_.asJValue) openOr (JNothing: JValue)
  def setFromJValue(jvalue: JValue): Box[SubRecordType] = jvalue match {
    case JNothing|JNull if optional_? => setBox(Empty)
    case _ => setBox(valueMeta.fromJValue(jvalue))
  }
}
