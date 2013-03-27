package com.softwaremill.codebrag.common

import org.bson.types.ObjectId

trait IdGenerator {
  def generateRandom(): ObjectId
}

class ObjectIdGenerator extends IdGenerator {

  override def generateRandom() = new ObjectId()
}

class FakeIdGenerator(val id: String) extends IdGenerator {

  override def generateRandom() = new ObjectId(id)
}