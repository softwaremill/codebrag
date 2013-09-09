package com.softwaremill.codebrag.service.email

import org.scalatest.{BeforeAndAfterAll, FlatSpec}
import org.scalatest.mock.MockitoSugar
import com.softwaremill.codebrag.service.config.EmailConfig
import com.softwaremill.codebrag.service.email.EmailSenderActor.SendEmail
import akka.actor.{Actor, ActorSystem}
import akka.testkit.{TestKit, TestActorRef}
import org.scalatest.matchers.ShouldMatchers
import org.mockito.Mockito._

class EmailSenderActorSpec extends FlatSpec with ShouldMatchers with MockitoSugar with BeforeAndAfterAll {

  val mockConfig = mock[EmailConfig]
  implicit val system = ActorSystem("testsystem")

  override def afterAll {
    system.shutdown()
  }


  val email: Email = new Email("address@email.com","subject", "content")


  "Actor" should "send email via EmailService" in {
    //given
    val mockService = mock[EmailService]
    val scheduler = mock[EmailScheduler]

    val actorRef = TestActorRef(new EmailSenderActor(mockService))

    //when
    actorRef.underlyingActor.asInstanceOf[Actor].receive(SendEmail(email, scheduler))

    //then
    verify(mockService).send(email)

  }

  "Actor" should "schedule email in case of failure" in {
    //given
    val mockService = mock[EmailService]
    when(mockService.send(email)).thenThrow(new EmailNotSendException("error", new RuntimeException))
    val scheduler = mock[EmailScheduler]


    val actorRef = TestActorRef(new EmailSenderActor(mockService))

    //when
    actorRef.underlyingActor.asInstanceOf[Actor].receive(SendEmail(email, scheduler))

    //then
    verify(scheduler).scheduleIn60Seconds(email)

  }

}