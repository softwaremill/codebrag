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

        $scope.addComment = {
            commitId: currentCommitId(),
            body: '',
            reset: function() {
                this.body = '';
            }
        };

        $scope.submitComment = function () {
            Comments.save($scope.addComment, function (commentResponse) {
                $scope.addComment.reset();
                $scope.commentsList.push(commentResponse.comment);
            })
        }

    })

