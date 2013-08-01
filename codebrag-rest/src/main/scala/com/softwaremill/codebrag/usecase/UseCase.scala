package com.softwaremill.codebrag.usecase

trait UseCase {

  def canExecute: Either[String, Boolean]

  def execute: Either[String, Unit]

}
