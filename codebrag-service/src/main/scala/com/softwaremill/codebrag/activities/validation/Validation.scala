package com.softwaremill.codebrag.activities.validation

import com.softwaremill.codebrag.activities.validation.Validation.ValidationRule


trait ValidateableForm {
  def validate: Validation
}

case class ValidationErrors(fieldErrors: Map[String, List[String]]) {
  def nonEmpty = fieldErrors.nonEmpty
  def isEmpty = fieldErrors.isEmpty
}

case class Validation(checks: List[ValidationRule]) {
  def this(checks: ValidationRule*) = this(checks.toList)
  def + (additionalChecks: ValidationRule*) = Validation(checks ++ additionalChecks)
  lazy val errors = ValidationErrors(checks.toList.filter(_._1).groupBy(_._3).mapValues(_.map(_._2)))
  def whenNoErrors[T](block: => T): Either[ValidationErrors, T] = if(errors.isEmpty) Right(block) else Left(errors)
}

object Validation {
  type ValidationRule = (Boolean, String, String)
}