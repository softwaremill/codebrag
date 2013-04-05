package com.softwaremill.codebrag.service.comments.command

import org.bson.types.ObjectId

case class AddComment(commitId: ObjectId, authorId: ObjectId, message: String)