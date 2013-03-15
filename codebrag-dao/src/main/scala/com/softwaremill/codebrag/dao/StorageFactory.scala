package com.softwaremill.codebrag.dao

trait StorageFactory {

  def userDAO: UserDAO

  def entryDAO: EntryDAO
}

class MongoFactory extends StorageFactory {

  def userDAO = {
    new MongoUserDAO
  }

  def entryDAO = {
    new MongoEntryDAO
  }

}

class InMemoryFactory() extends StorageFactory {

  def userDAO = {
    new InMemoryUserDAO
  }

  def entryDAO = {
    new InMemoryEntryDAO()
  }

}

