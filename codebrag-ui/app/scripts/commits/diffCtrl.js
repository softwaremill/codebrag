angular.module('codebrag.commits')

    .controller('DiffCtrl', function ($scope, Comments, Likes, authService, $q) {

        $scope.like = function(fileName, lineNumber) {
            var currentUser = authService.loggedInUser;
            if(_userCannotLikeThisLine(currentUser, fileName, lineNumber)) {
                return $q.reject();
            }
            var newLike = {
                commitId: $scope.currentCommit.info.id,
                fileName: fileName,
                lineNumber: lineNumber
            };
            return Likes.save(newLike).$then(function (likeResponse) {
                var like = likeResponse.data;
                $scope.currentCommit.addLike(like, fileName, lineNumber);
            });
        };

        $scope.unlike = function(fileName, lineNumber) {
            var currentUser = authService.loggedInUser;
            if(_userCannotUnlikeThisLine(currentUser, fileName, lineNumber)) {
                return $q.reject();
            }
            var likeToRemove = $scope.currentCommit.findLikeFor(currentUser, fileName, lineNumber);
            if(likeToRemove) {
                var requestData = {
                    commitId: $scope.currentCommit.info.id,
                    likeId: likeToRemove.id
                };
                return Likes.delete(requestData).$then(function() {
                    $scope.currentCommit.removeLike(fileName, lineNumber, likeToRemove.id);
                });
            } else {
                return $q.reject();
            }
        };

        function _userCannotLikeThisLine(user, fileName, lineNumber) {
            return $scope.currentCommit.isUserAuthorOfCommit(user) || $scope.currentCommit.userAlreadyLikedLine(user, fileName, lineNumber);
        }

        function _userCannotUnlikeThisLine(user, fileName, lineNumber) {
            return !$scope.currentCommit.userAlreadyLikedLine(user, fileName, lineNumber);
        }

        $scope.submitInlineComment = function(content, commentData) {
            var newComment = {
                commitId: $scope.currentCommit.info.id,
                body: content,
                fileName: commentData.fileName,
                lineNumber: commentData.lineNumber
            };

            return Comments.save(newComment).$then(function (commentResponse) {
                var comment = commentResponse.data.comment;
                $scope.currentCommit.addInlineComment(comment, commentData.fileName, commentData.lineNumber);
            });
        };

        $scope.submitComment = function (content) {
            var comment = {
                commitId: $scope.currentCommit.info.id,
                body: content
            };
            return Comments.save(comment).$then(function (commentResponse) {
                var comment = commentResponse.data.comment;
                $scope.currentCommit.addComment(comment);
            });
        };

    });


