package com.softwaremill.codebrag.tools

import com.typesafe.config.ConfigFactory
import com.softwaremill.codebrag.common.Utils
import java.io.File
import com.softwaremill.codebrag.domain.User
import com.softwaremill.codebrag.dao.user.{UserDAO, SQLUserDAO}
import com.softwaremill.codebrag.dao.DaoConfig
import com.softwaremill.codebrag.dao.sql.SQLDatabase
import org.slf4j.LoggerFactory
import ch.qos.logback.classic.{Level, Logger}
import org.slf4j

object ChangeUserPassword {

  case class UserPassword(username: String, newPassword: String) {
    def invalid = {
      username == null || newPassword == null || username.isEmpty || newPassword.isEmpty
    }
  }

  def main(args: Array[String]) {
    disableLogging
    haltIfConfigFileDoesntExist

    println("##########################################################")
    println("# Super-awesome tool to change Codebrag user's password  #")
    println("#                                                        #")
    println("# NOTE: Make sure Codebrag is stopped before using it    #")
    println("# Otherwise you'll get 'Locked by another process' error #")
    println("#         Yes, we know it's not the best way ;)          #")
    println("##########################################################")
    val userData = collectInput
    withDB { db =>
      changePasswordIfUserExists(userData, db)
    }
    println("Bye!")
  }


  def disableLogging {
    val logger = LoggerFactory.getLogger(slf4j.Logger.ROOT_LOGGER_NAME).asInstanceOf[Logger]
    logger.setLevel(Level.OFF)
  }

  private def changePasswordIfUserExists(userData: ChangeUserPassword.UserPassword, db: SQLDatabase) {
    val userDao = new SQLUserDAO(db)
    userDao.findByLoginOrEmail(userData.username) match {
      case Some(user) => {
        updateUserRecord(userData.newPassword, user, userDao)
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


  private def updateUserRecord(newPassword: String, user: User, userDao: UserDAO) {
    val newPasswordHashed = Utils.sha256(newPassword, user.authentication.salt)
    val auth = user.authentication.copy(token = newPasswordHashed)
    userDao.changeAuthentication(user.id, auth)
    println("User password succesfully changed")
  }

  private def withDB(doWithDb: SQLDatabase => Unit) {
    val config = new DaoConfig {
      def rootConfig = ConfigFactory.load()
    }
    val db = SQLDatabase.createEmbedded(config)
    try {
      doWithDb(db)
    } finally {
      db.close()      
    }
  }

  private def haltIfConfigFileDoesntExist {
    val configFile = System.getProperty("config.file")
    if (!new File(configFile).exists()) {
      println(s"Cannot find config file at $configFile")
      System.exit(1)
    }
  }
}
