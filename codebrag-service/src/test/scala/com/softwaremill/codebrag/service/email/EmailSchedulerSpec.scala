package com.softwaremill.codebrag.service.email

import org.scalatest.{BeforeAndAfterAll, FlatSpec}
import org.scalatest.mock.MockitoSugar
import akka.actor._
import org.scalatest.matchers.ShouldMatchers
import com.softwaremill.codebrag.service.email.EmailSenderActor.SendEmail
import scala.concurrent.duration._
import com.softwaremill.codebrag.service.email.EmailSchedulerSpec.MemoActor

class EmailSchedulerSpec extends FlatSpec with MockitoSugar with ShouldMatchers with BeforeAndAfterAll {

  implicit val system = ActorSystem("testsystem")

  override def afterAll {
    system.shutdown()
  }

  val delay: FiniteDuration = 5.seconds
  val overhead: Int = 500

  val memo = system.actorOf(Props[MemoActor])
  val scheduler: EmailScheduler = new EmailScheduler(system, memo)

  val email: Email = new Email(List("test@test.pl"), "subject", "content")

  it should " schedule and send message to actor instantly" in {
    import EmailSchedulerSpec._
    //given
    message = None

    //when
    scheduler.scheduleInstant(email)
    Thread.sleep(overhead)

    //then
    message should be(Some(SendEmail(email, scheduler)))

  }

  it should s" reschedule and send message to actor (in $delay)" in {
    import EmailSchedulerSpec._
    //given
    message = None
    val scheduler: EmailScheduler = new EmailScheduler(system, memo)

    //when
    scheduler.schedule(email, delay)

    //then
    message should be(None)
    Thread.sleep(delay.toMillis/2 + overhead)
    message should be(None)
    Thread.sleep(delay.toMillis/2 + overhead)

    message should be(Some(SendEmail(email, scheduler)))
  }

}

object EmailSchedulerSpec {
  var message: Option[SendEmail] = None

  class MemoActor extends Actor {
    def receive = {
      case y: SendEmail ⇒ message = Some(y)
    }
  }

}