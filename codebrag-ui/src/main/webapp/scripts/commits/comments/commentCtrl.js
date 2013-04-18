'use strict';

angular.module('codebrag.commits.comments')

    .controller('CommentCtrl',function ($scope, $stateParams, Comments) {

        $scope.commentsList = [];

        loadAllCommentsForCurrentCommit();

        function currentCommitId() {
            return $stateParams.id;
        }

        function loadAllCommentsForCurrentCommit() {
            Comments.query({id: currentCommitId()}, function(data) {
                $scope.commentsList = data.comments;
            })
        }

        $scope.submitComment = function (content) {
            var comment = {
                commitId: currentCommitId(),
                body: content
            };
            Comments.save(comment, function (commentResponse) {
                $scope.commentsList.push(commentResponse.comment);
                $scope.$broadcast('codebrag:commentCreated');
            })
        }

    });
