package com.softwaremill.codebrag.dao

import org.bson.types.ObjectId

object ObjectIdTestUtils {
  implicit def intSuffixToObjId(number: Int) = new ObjectId(intSuffixToStringId(number))
  implicit def intSuffixToStringId(number: Int) = "507f191e810c19729de860e" + number

}
