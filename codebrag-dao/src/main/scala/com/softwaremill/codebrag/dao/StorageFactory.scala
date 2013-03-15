package com.softwaremill.codebrag.dao

trait StorageFactory {

  def userDAO: UserDAO
}

class MongoFactory extends StorageFactory {

  def userDAO = {
    new MongoUserDAO
  }

}

class InMemoryFactory() extends StorageFactory {

  def userDAO = {
    new InMemoryUserDAO
  }

}

