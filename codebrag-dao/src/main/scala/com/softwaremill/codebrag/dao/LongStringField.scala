package com.softwaremill.codebrag.dao

import net.liftweb.record.Record
import net.liftweb.record.field.{OptionalStringField, StringField}

class LongStringField[OwnerType <: Record[OwnerType]](rec: OwnerType, maxLen: Int = Int.MaxValue) extends StringField(rec, maxLen)

class OptionalLongStringField[OwnerType <: Record[OwnerType]](rec: OwnerType, maxLen: Int = Int.MaxValue) extends OptionalStringField(rec, maxLen)
