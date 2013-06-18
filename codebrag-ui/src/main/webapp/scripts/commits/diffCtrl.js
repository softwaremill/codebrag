angular.module('codebrag.commits')

    .controller('DiffCtrl', function ($scope, Comments, Likes, authService) {


        $scope.like = function(fileName, lineNumber) {
            var currentUserName = authService.loggedInUser.fullName;
            var reactions = $scope.currentCommit.lineReactions;
            if(reactions[fileName] && reactions[fileName][lineNumber] && reactions[fileName][lineNumber]['likes']) {
                var currentUserAlreadyLiked = _.some(reactions[fileName][lineNumber]['likes'], function(like) {
                    return like.authorName === currentUserName;
                });
                if(currentUserAlreadyLiked) {
                    return;
                }
            }

            var newLike = {
                commitId: $scope.currentCommit.commit.id,
                fileName: fileName,
                lineNumber: lineNumber
            };
            return Likes.save(newLike).$then(function (likeResponse) {
                var like = likeResponse.data;
                addLikeToCollection(like, newLike.fileName, newLike.lineNumber);
            });

            function addLikeToCollection(like, fileName, lineNumber) {
                var reactions = $scope.currentCommit.lineReactions;
                if(_.isUndefined(reactions[fileName])) {
                    reactions[fileName] = {};
                }
                if(_.isUndefined(reactions[fileName][lineNumber])) {
                    reactions[fileName][lineNumber] = [];
                }
                if(_.isUndefined(reactions[fileName][lineNumber]['likes'])) {
                    reactions[fileName][lineNumber]['likes'] = [];
                }
                reactions[fileName][lineNumber]['likes'].push(like);
            }
        };

        $scope.submitInlineComment = function(content, commentData) {
            var newComment = {
                commitId: $scope.currentCommit.commit.id,
                body: content,
                fileName: commentData.fileName,
                lineNumber: commentData.lineNumber
            };

            return Comments.save(newComment).$then(function (commentResponse) {
                var comment = commentResponse.data.comment;
                addCommentToCommentsCollection(comment, newComment.fileName, newComment.lineNumber);
            });

            function addCommentToCommentsCollection(comment, fileName, lineNumber) {
                var reactions = $scope.currentCommit.lineReactions;
                if(_.isUndefined(reactions[fileName])) {
                    reactions[fileName] = {};
                }
                if(_.isUndefined(reactions[fileName][lineNumber])) {
                    reactions[fileName][lineNumber] = [];
                }
                if(_.isUndefined(reactions[fileName][lineNumber]['comments'])) {
                    reactions[fileName][lineNumber]['comments'] = [];
                }
                reactions[fileName][lineNumber]['comments'].push(comment);
            }
        };

        $scope.submitComment = function (content) {
            var comment = {
                commitId: $scope.currentCommit.commit.id,
                body: content
            };
            return Comments.save(comment).$then(function (commentResponse) {
                $scope.currentCommit.reactions.comments.push(commentResponse.data.comment);
            });
        };

    });


