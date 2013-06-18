var codebrag = codebrag || {};

codebrag.CommitReactions = function(reactions, lineReactions) {

    this.reactions = reactions;
    this.lineReactions = lineReactions;

};

codebrag.CommitReactions.prototype = {

    addLike: function(like, fileName, lineNumber) {
        this._ensureReactionsCollectionExists(fileName, lineNumber, 'likes');
        this.lineReactions[fileName][lineNumber]['likes'].push(like);
    },

    addInlineComment: function(comment, fileName, lineNumber) {
        this._ensureReactionsCollectionExists(fileName, lineNumber, 'comments');
        this.lineReactions[fileName][lineNumber]['comments'].push(comment);
    },

    addComment: function(comment) {
        if(_.isUndefined(this.reactions['comments'])) {
            this.reactions['comments'] = [];
        }
        this.reactions['comments'].push(comment);
    },

    userAlreadyLikedThis: function(userName, fileName, lineNumber) {
        if(this.lineReactions[fileName] && this.lineReactions[fileName][lineNumber] && this.lineReactions[fileName][lineNumber]['likes']) {
            return _.some(this.lineReactions[fileName][lineNumber]['likes'], function(like) {
                return like.authorName === userName;
            });
        }
        return false;
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