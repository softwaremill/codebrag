package com.softwaremill.codebrag.eventstream

import java.io.StringWriter
import java.net.InetSocketAddress

import akka.actor.ActorSystem
import akka.testkit.{TestActorRef, TestKit}
import com.softwaremill.codebrag.common.ClockSpec
import com.softwaremill.codebrag.dao.commitinfo.CommitInfoDAO
import com.softwaremill.codebrag.dao.events.NewUserRegistered
import com.softwaremill.codebrag.dao.user.UserDAO
import com.softwaremill.codebrag.domain._
import com.softwaremill.codebrag.domain.reactions._
import com.sun.net.httpserver._
import org.bson.types.ObjectId
import org.mockito.Mockito._
import org.scalatest.matchers.MustMatchers
import org.scalatest.mock.MockitoSugar
import org.scalatest.{BeforeAndAfter, WordSpec}

import scala.collection.convert.Wrappers._

class EventHookPropagatorSpec
  extends TestKit(ActorSystem("testSystem"))
  with WordSpec
  with BeforeAndAfter
  with MustMatchers
  with ClockSpec
  with MockitoSugar
{

  var remoteHost: HttpServer = null
  var remoteHook: StringWriter = null
  var headers: Headers = null
  var done = false

  var mockUserDao = mock[UserDAO]
  var mockCommitInfoDao = mock[CommitInfoDAO]

  val id = ObjectId.get

  val nowUtc = clock.now
  val nowUtcStr = nowUtc.toString("yyyy-MM-dd'T'HH:mm:ss'Z'")

  val commitId = id
  val mockCommitInfo = CommitInfo(id, "test", "szach", "test-message", "mocher", "mocher@domain.com", "mocher", "mocher@domain.com", nowUtc, nowUtc, List())

  val userId = id
  val mockUser = User(userId, null, "test", "test@domain.com", Set(PlainUserToken("123456789").hashed), admin = false, active = true, null, null, null)

  val mockLike = Like(id, commitId, userId, nowUtc, Some("test.txt"), Some(123))
  val mockComment = Comment(id, id, id, nowUtc, "test-comment", Some("test2.txt"), Some(321))
  val mockPartialCommit = PartialCommitInfo(id, "szach", "commit-loaded", "mocher", "mocher@domain.com", nowUtc)

  val hooks = Map(
    "like-hook" -> List("http://localhost:8000/"),
    "unlike-hook" -> List("http://localhost:8000/"),
    "comment-added-hook" -> List("http://localhost:8000/"),
    "commit-reviewed-hook" -> List("http://localhost:8000/"),
    "new-commits-loaded-hook" -> List("http://localhost:8000/"),
    "new-user-registered-hook" -> List("http://localhost:8000/")
  )

  val actorRef = TestActorRef(new EventHookPropagator(hooks, mockCommitInfoDao, mockUserDao))

  "EventHookPropagator" must {

    "propagate LikeEvent to remote host" in {
      actorRef ! LikeEvent(mockLike)

      awaitCond(done)

      val expected = s"""
        {
          "commitInfo": {
            "repoName": "test",
            "sha": "szach",
            "message": "test-message",
            "authorName": "mocher",
            "authorEmail": "mocher@domain.com",
            "committerName": "mocher",
            "committerEmail": "mocher@domain.com",
            "authorDate": "$nowUtcStr",
            "commitDate": "$nowUtcStr",
            "parents": []
          },
          "likedBy": {
            "name": "test",
            "emailLowerCase": "test@domain.com",
            "aliases": null
          },
          "like": {
            "postingTime": "$nowUtcStr",
            "fileName": "test.txt",
            "lineNumber": 123
          },
          "hookName": "like-hook",
          "hookDate": "$nowUtcStr"
        }
        """.replaceAll(" ", "").replaceAll("\n", "")

      remoteHook.toString must include(expected)
    }

    "propagate UnlikeEvent to remote host" in {
      actorRef ! UnlikeEvent(mockLike)

      awaitCond(done)

      val expected = s"""
        {
          "commitInfo": {
            "repoName": "test",
            "sha": "szach",
            "message": "test-message",
            "authorName": "mocher",
            "authorEmail": "mocher@domain.com",
            "committerName": "mocher",
            "committerEmail": "mocher@domain.com",
            "authorDate": "$nowUtcStr",
            "commitDate": "$nowUtcStr",
            "parents": []
          },
          "unlikedBy": {
            "name": "test",
            "emailLowerCase": "test@domain.com",
            "aliases": null
          },
          "like": {
            "postingTime": "$nowUtcStr",
            "fileName": "test.txt",
            "lineNumber": 123
          },
          "hookName": "unlike-hook",
          "hookDate": "$nowUtcStr"
        }
        """.replaceAll(" ", "").replaceAll("\n", "")

      remoteHook.toString must include(expected)
    }

    "propagate CommentAddedEvent to remote host" in {
      actorRef ! CommentAddedEvent(mockComment)

      awaitCond(done)

      val expected = s"""
        {
          "commitInfo": {
            "repoName": "test",
            "sha": "szach",
            "message": "test-message",
            "authorName": "mocher",
            "authorEmail": "mocher@domain.com",
            "committerName": "mocher",
            "committerEmail": "mocher@domain.com",
            "authorDate": "$nowUtcStr",
            "commitDate": "$nowUtcStr",
            "parents": []
          },
          "commentedBy": {
            "name": "test",
            "emailLowerCase": "test@domain.com",
            "aliases": null
          },
          "comment": {
            "message": "test-comment",
            "postingTime": "$nowUtcStr",
            "fileName": "test2.txt",
            "lineNumber": 321
          },
          "hookName": "comment-added-hook",
          "hookDate": "$nowUtcStr"
        }
        """.replaceAll(" ", "").replaceAll("\n", "")

      remoteHook.toString must include(expected)
    }

    "propagate CommitReviewed to remote host" in {
      actorRef ! CommitReviewedEvent(mockCommitInfo, userId)

      awaitCond(done)

      val expected = s"""
        {
          "commitInfo": {
            "repoName": "test",
            "sha": "szach",
            "message": "test-message",
            "authorName": "mocher",
            "authorEmail": "mocher@domain.com",
            "committerName": "mocher",
            "committerEmail": "mocher@domain.com",
            "authorDate": "$nowUtcStr",
            "commitDate": "$nowUtcStr",
            "parents": []
          },
          "reviewedBy": {
            "name": "test",
            "emailLowerCase": "test@domain.com",
            "aliases": null
          },
          "hookName": "commit-reviewed-hook",
          "hookDate": "$nowUtcStr"
        }
        """.replaceAll(" ", "").replaceAll("\n", "")

      remoteHook.toString must include(expected)
    }

    "propagate NewCommitsLoadedEvent to remote host" in {
      actorRef ! NewCommitsLoadedEvent(firstTime = false, "test", "szach", List(mockPartialCommit))

      awaitCond(done)

      val expected = s"""
        {
          "repoName": "test",
          "currentSHA": "szach",
          "newCommits": [
            {
              "sha": "szach",
              "message": "commit-loaded",
              "authorName": "mocher",
              "authorEmail": "mocher@domain.com",
              "date": "$nowUtcStr"
            }
          ],
          "hookName": "new-commits-loaded-hook",
          "hookDate": "$nowUtcStr"
        }
        """.replaceAll(" ", "").replaceAll("\n", "")

      remoteHook.toString must include(expected)
    }

    "propagate NewUserRegistered to remote host" in {
      actorRef ! NewUserRegistered(id, "super", "Master-Disaster", "master@domain.com")

      awaitCond(done)

      val expected = s"""
        {
          "newUser": {
            "name": "test",
            "emailLowerCase": "test@domain.com",
            "aliases": null
          },
          "login": "super",
          "fullName": "Master-Disaster",
          "hookName": "new-user-registered-hook",
          "hookDate": "$nowUtcStr"
        }
        """.replaceAll(" ", "").replaceAll("\n", "")

      remoteHook.toString must include(expected)
    }

    "propagated hook must be UTF-8 encoded" in {

      actorRef ! NewUserRegistered(id, "super", "Master-Disaster", "master@domain.com")

      awaitCond(done)

      val contentType = JListWrapper(headers.get("Content-Type")).mkString(";")

      contentType must include("application/json")
      contentType must include("UTF-8")
    }

    "must not create hook if no listeners were defined" in {
      import scala.concurrent.duration._

      reset(mockUserDao)
      val actorRef = TestActorRef(new EventHookPropagator(Map(), mockCommitInfoDao, mockUserDao))

      actorRef ! NewUserRegistered(id, "super", "Master-Disaster", "master@domain.com")

      receiveOne(1.second)

      verifyZeroInteractions(mockUserDao)
    }

  }

  before {
    remoteHook = new StringWriter

    remoteHost = HttpServer.create(new InetSocketAddress(8000), 0)
    remoteHost.createContext("/", new HttpHandler() {
      override def handle(t: HttpExchange): Unit = {
        val in = t.getRequestBody

        Iterator
          .continually(in.read)
          .takeWhile(-1 != _)
          .foreach(remoteHook.write)

        headers = t.getRequestHeaders

        done = true

        val response = "Ack!"
        t.sendResponseHeaders(200, response.length())
        val os = t.getResponseBody
        os.write(response.getBytes)
        os.close()
      }
    })
    remoteHost.setExecutor(null)
    remoteHost.start()

    when(mockUserDao.findById(userId)).thenReturn(Some(mockUser))
    when(mockCommitInfoDao.findByCommitId(commitId)).thenReturn(Some(mockCommitInfo))
  }

  after{
    done = false
    remoteHost.stop(0)
  }

}
