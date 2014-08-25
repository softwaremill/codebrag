package com.softwaremill.codebrag.licence

import org.joda.time.format.DateTimeFormat
import org.joda.time.DateTime

/**
 * Super awesome licence generation tool
 */
object LicenceGeneratorApp extends App {

  private val generator = new LicenceGenerator
  private val Formatter = DateTimeFormat.forPattern("dd/MM/yyyy")

  println("--------------------------------")
  println("Codebrag licence generation tool")
  println("--------------------------------")

  val date = enterExpirationDate
  val users = enterUsersCount
  val company = enterCompanyName

  val licence = generator.generateLicence(date, users, company)

  println
  println("Generated licence key:")
  println(licence)
  println("Thanks, exiting.")

  private def enterExpirationDate: DateTime = {
    print("Enter expiration date (dd/MM/yyyy)> ")
    val expDateString = readLine()
    try {
      DateTime.parse(expDateString, Formatter)
    } catch {
      case e: Exception => {
        println(s"Invalid date: ${expDateString}")
        enterExpirationDate
      }
    }
  }

  private def enterUsersCount: Int = {
    print("Enter users count> ")
    val usersCountString = readLine()
    try {
      usersCountString.toInt
    } catch {
      case e: Exception => {
        println(s"Invalid users count: ${usersCountString}")
        enterUsersCount
      }
    }
  }

  private def enterCompanyName: String = {
    print("Enter company name > ")
    val companyName = readLine()
    if(companyName.trim.isEmpty) {
      println("Company name is required")
      enterCompanyName
    }
    companyName
  }
}
