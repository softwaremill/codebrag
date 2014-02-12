package com.softwaremill.codebrag.dao.followup

import com.softwaremill.codebrag.dao.reaction.{LikeDAO, CommitCommentDAO}
import org.bson.types.ObjectId
import com.softwaremill.codebrag.domain.{UserReaction, FollowupWithReactions, FollowupWithNoReactions, ThreadDetails}
import com.foursquare.rogue.LiftRogue._

class MongoFollowupWithReactionsDAO(commentsDao: CommitCommentDAO, likeDao: LikeDAO) extends FollowupWithReactionsDAO {

   def findById(followupId: ObjectId) = {
     FollowupRecord.where(_.id eqs followupId).get().map(buildDomainObject)
   }

   private def buildDomainObject(followup: FollowupRecord) = {
     val threadRecord = followup.threadId.get
     val thread = ThreadDetails(threadRecord.commitId.get, threadRecord.lineNumber.get, threadRecord.fileName.get)
     val allReactions = commentsDao.findAllCommentsForThread(thread) ++ likeDao.findAllLikesForThread(thread)
     if(allReactions.isEmpty) {
       Left(FollowupWithNoReactions(followup.id.get, followup.receivingUserId.get, thread))
     } else {
       val lastReaction = determineLastReaction(allReactions)
       Right(FollowupWithReactions(followup.id.get, followup.receivingUserId.get, thread, lastReaction, allReactions))
     }
   }

   def determineLastReaction(allReactions: List[UserReaction with Product with Serializable]) = {
     allReactions.max(new Ordering[UserReaction] {
       def compare(x: UserReaction, y: UserReaction): Int = x.postingTime.compareTo(y.postingTime)
     })
   }

   def findAllContainingReaction(reactionId: ObjectId) = {
     FollowupRecord.where(_.reactions contains reactionId).fetch().map(buildDomainObject)
   }

   def update(followup: FollowupWithReactions) {
     FollowupRecord.where(_.id eqs followup.followupId).findAndModify(_.lastReaction.subfield(_.reactionId) setTo followup.lastReaction.id)
       .and(_.lastReaction.subfield(_.reactionAuthorId) setTo followup.lastReaction.authorId)
       .and(_.reactions setTo followup.allReactions.map(_.id)).updateOne(true)
   }

 }
