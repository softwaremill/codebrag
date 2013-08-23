var codebrag = codebrag || {};

codebrag.CurrentCommit = function(commitData) {

    this.reactions = commitData.reactions;
    this.reactions.comments = this.reactions.comments || [];
    this.reactions.likes = this.reactions.likes || [];
    this.lineReactions = commitData.lineReactions;

    this.info = commitData.commit;

    this.diff = commitData.diff;

    this.supressedFiles = commitData.supressedFiles;

};

codebrag.CurrentCommit.prototype = {

    addLike: function(like, fileName, lineNumber) {
        this._addLineReaction(like, 'likes', fileName, lineNumber)
    },

    addInlineComment: function(comment, fileName, lineNumber) {
        this._addLineReaction(comment, 'comments', fileName, lineNumber)
    },

    _addLineReaction: function(reaction, reactionType, fileName, lineNumber) {
        this._ensureReactionsCollectionExists(fileName, lineNumber, reactionType);
        this.lineReactions[fileName][lineNumber][reactionType].push(reaction);
    },

    removeLike: function(fileName, lineNumber, likeId) {
        this._ensureReactionsCollectionExists(fileName, lineNumber, 'likes');
        var lineLikes = this.lineReactions[fileName][lineNumber]['likes'];
        var likeToRemove = _.find(lineLikes, function(like) {
            return like.id === likeId;
        });
        lineLikes.splice(lineLikes.indexOf(likeToRemove), 1);
    },

    findLikeFor: function(userName, fileName, lineNumber) {
        if(fileName && lineNumber) {
            this._ensureReactionsCollectionExists(fileName, lineNumber, 'likes');
            var lineLikes = this.lineReactions[fileName][lineNumber]['likes'];
            return _.find(lineLikes, function(like) {
                return like.authorName === userName;
            });
        } else {
            return _.find(this.reactions.likes, function(like) {
                return like.authorName === userName;
            })
        }
    },

    addComment: function(comment) {
        this._addReaction('comments', comment);
    },

    _addReaction: function(reactionType, reactionObject) {
    if(_.isUndefined(this.reactions[reactionType])) {
        this.reactions[reactionType] = [];
    }
    this.reactions[reactionType].push(reactionObject);
    },

    isUserAuthorOfCommit: function(userName) {
         return this.info.authorName === userName
    },

    userAlreadyLikedLine: function (userName, fileName, lineNumber) {
        if (this._hasAnyLikesForLine(fileName, lineNumber)) {
            return this._containsLikeWithUserName(userName, this._likesForLine(fileName, lineNumber));
        }
        return false;
    },
    _hasAnyLikesForLine: function (fileName, lineNumber) {
        return this.lineReactions[fileName] && this.lineReactions[fileName][lineNumber] && this._likesForLine(fileName, lineNumber);
    },
    _likesForLine: function (fileName, lineNumber) {
        return this.lineReactions[fileName][lineNumber]['likes'];
    },
    _containsLikeWithUserName: function (userName, collection) {
        return _.some(collection, function (like) {
            return like.authorName === userName;
        });
    },
    addGeneralLike: function(like) {
        this._addReaction('likes', like);
    },

    userAlreadyLikedCommit: function (userName) {
        if (this._hasAnyGeneralLikes()) {
            return this._containsLikeWithUserName(userName, this.reactions['likes']);
        }
        return false;
    },

    _hasAnyGeneralLikes: function () {
        return !(_.isUndefined(this.reactions['likes']) || _.isEmpty(this.reactions['likes']));
    },

    _ensureReactionsCollectionExists: function(fileName, lineNumber, reactionType) {
        if(_.isUndefined(this.lineReactions[fileName])) {
            this.lineReactions[fileName] = {};
        }
        if(_.isUndefined(this.lineReactions[fileName][lineNumber])) {
            this.lineReactions[fileName][lineNumber] = [];
        }
        if(_.isUndefined(this.lineReactions[fileName][lineNumber][reactionType])) {
            this.lineReactions[fileName][lineNumber][reactionType] = [];
        }
    }

};