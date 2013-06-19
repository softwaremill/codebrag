angular.module('codebrag.commits')

    .controller('DiffCtrl', function ($scope, Comments, Likes, authService) {


        $scope.like = function(fileName, lineNumber) {
            var currentUserName = authService.loggedInUser.fullName;
            if($scope.currentCommit.isUserAuthorOfCommit(currentUserName) || $scope.currentCommit.userAlreadyLikedLine(currentUserName, fileName, lineNumber)) {
                return;
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


