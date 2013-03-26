package com.softwaremill.codebrag.common

import java.util.UUID

trait IdGenerator {
  def generateRandom(): String
}

class UuidGenerator extends IdGenerator {

  def generateRandom(): String = UUID.randomUUID().toString
}

class FakeIdGenerator(val id: String) extends IdGenerator {
  def generateRandom(): String = id
}