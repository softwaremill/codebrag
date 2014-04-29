package com.softwaremill.codebrag.instance

import org.scalatest.{BeforeAndAfter, FlatSpec}
import org.scalatest.matchers.ShouldMatchers
import org.scalatest.mock.MockitoSugar
import com.softwaremill.codebrag.dao.instance.InstanceParamsDAO
import com.softwaremill.codebrag.domain.{InstanceParam, InstanceId}
import org.mockito.Mockito._
import org.mockito.Matchers._

/**
 * Created by michal on 29.04.14.
 */
class InstanceParamsServiceSpec extends FlatSpec with ShouldMatchers with BeforeAndAfter with MockitoSugar {

  var service: InstanceParamsService = _
  var dao: InstanceParamsDAO = _

  val DummyInstanceId = InstanceId("12345")

  before {
    dao = mock[InstanceParamsDAO]
  }

  it should "return instance ID from database if exists" in {
    // given
    when(dao.findByKey(InstanceId.Key)).thenReturn(Some(DummyInstanceId.toInstanceParam))
    service = new InstanceParamsService(dao)

    // when
    val result = service.readOrCreateInstanceId

    // then
    result should be(DummyInstanceId)
    verify(dao, times(0)).save(any[InstanceParam])
  }

  it should "import instance ID from file to database and return if one doesn't exists in database but exists as file" in {
    // given
    when(dao.findByKey(InstanceId.Key)).thenReturn(None)
    service = new InstanceParamsService(dao) {
      override def loadExistingInstanceIdFromFile = Some(DummyInstanceId)
    }

    // when
    val result = service.readOrCreateInstanceId

    // then
    result should be(DummyInstanceId)
    verify(dao).save(DummyInstanceId.toInstanceParam)
  }

  it should "create new instanceId in database and return if one doesn't exists in database and doesn't exists as file" in {
    // given
    when(dao.findByKey(InstanceId.Key)).thenReturn(None)
    service = new InstanceParamsService(dao) {
      override def loadExistingInstanceIdFromFile = None
    }

    // when
    val result = service.readOrCreateInstanceId

    // then
    verify(dao).save(result.toInstanceParam)
  }

  it should "throw exception when instance ID cannot be read" in {
    // given
    when(dao.findByKey(InstanceId.Key)).thenThrow(new RuntimeException)
    service = new InstanceParamsService(dao)

    // when
    intercept[RuntimeException] {
      service.readOrCreateInstanceId
    }
  }

  it should "throw exception when instance ID cannot be created" in {
    // given
    when(dao.save(any[InstanceParam])).thenThrow(new RuntimeException)
    service = new InstanceParamsService(dao)

    // when
    intercept[RuntimeException] {
      service.readOrCreateInstanceId
    }
  }

}