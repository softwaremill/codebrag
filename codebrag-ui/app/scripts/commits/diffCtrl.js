angular.module('codebrag.commits')

    .controller('DiffCtrl', function ($scope, Comments, Likes, authService, $q) {

        $scope.like = function(fileName, lineNumber) {
            var currentUserName = authService.loggedInUser.fullName;
            if(_userCannotLikeThisLine(currentUserName, fileName, lineNumber)) {
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
            var currentUserName = authService.loggedInUser.fullName;
            if(_userCannotUnlikeThisLine(currentUserName, fileName, lineNumber)) {
                return $q.reject();
            }
            var userId = authService.loggedInUser.id;
            var likeToRemove = $scope.currentCommit.findLikeFor(currentUserName, fileName, lineNumber);
            if(likeToRemove) {
                var requestData = {
                    commitId: $scope.currentCommit.info.id,
                    likeId: likeToRemove.id
                };
                return Likes.delete(requestData).$then(function(resp) {
                    $scope.currentCommit.removeLike(fileName, lineNumber, likeToRemove.id);
                });
            } else {
                return $q.reject();
            }
        };

        function _userCannotLikeThisLine(currentUserName, fileName, lineNumber) {
            return $scope.currentCommit.isUserAuthorOfCommit(currentUserName) || $scope.currentCommit.userAlreadyLikedLine(currentUserName, fileName, lineNumber);
        }

        function _userCannotUnlikeThisLine(currentUserName, fileName, lineNumber) {
            return !$scope.currentCommit.userAlreadyLikedLine(currentUserName, fileName, lineNumber);
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


