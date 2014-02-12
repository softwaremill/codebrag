package com.softwaremill.codebrag.tools

import com.typesafe.config.ConfigFactory
import com.softwaremill.codebrag.common.Utils
import java.io.File
import com.softwaremill.codebrag.domain.User
import com.softwaremill.codebrag.dao.user.MongoUserDAO
import com.softwaremill.codebrag.dao.mongo.MongoInit
import com.softwaremill.codebrag.dao.DaoConfig

object ChangeUserPassword {

  case class UserPassword(username: String, newPassword: String) {
    def invalid = {
      username == null || newPassword == null || username.isEmpty || newPassword.isEmpty
    }
  }

  def main(args: Array[String]) {

    haltIfConfigFileDoesntExist

    println("#########################################################")
    println("# Super-awesome tool to change Codebrag user's password #")
    println("#########################################################")
    val userData = collectInput
    initializeMongoConnection
    changePasswordIfUserExists(userData)
    println("Bye!")
  }


  private def changePasswordIfUserExists(userData: ChangeUserPassword.UserPassword) {
    (new MongoUserDAO).findByLoginOrEmail(userData.username) match {
      case Some(user) => {
        updateUserRecord(userData.newPassword, user, new MongoUserDAO)
      }
      case None => {
        val username = userData.username
        println(s"User $username not found. Password not changed")
      }
    }
  }

  private def collectInput: UserPassword = {
    val console = System.console()
    val username = console.readLine("Enter username to change password for> ")
    val newPassword = console.readPassword("Enter new password (not displayed)> ")
    val userData = UserPassword(username, String.copyValueOf(newPassword))
    if(userData.invalid) {
      println("Provide non-empty values for username and new password")
      System.exit(1)      
    }
    userData
  }


  private def updateUserRecord(newPassword: String, user: User, userDao: MongoUserDAO) {
    val newPasswordHashed = Utils.sha256(newPassword, user.authentication.salt)
    val auth = user.authentication.copy(token = newPasswordHashed)
    userDao.changeAuthentication(user.id, auth)
    println("User password succesfully changed")
  }

  private def initializeMongoConnection {
    val config = new DaoConfig {
      def rootConfig = ConfigFactory.load()
    }
    MongoInit.initializeWithoutIndexCheck(config)
  }

  private def haltIfConfigFileDoesntExist {
    val configFile = System.getProperty("config.file")
    if (!new File(configFile).exists()) {
      println(s"Cannot find config file at $configFile")
      System.exit(1)
    }
  }
}
