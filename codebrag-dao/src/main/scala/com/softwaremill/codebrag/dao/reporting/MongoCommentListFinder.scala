package com.softwaremill.codebrag.dao.reporting

import org.bson.types.ObjectId

class MongoCommentListFinder extends CommentListFinder {

  override def findAllForCommit(commitId: ObjectId) = {
    CommentListDTO(List.empty);
  }
}
