'use strict';

angular.module('codebrag.commits.comments')

    .controller('CommentCtrl',function ($scope, currentCommit, Comments) {

        $scope.commentsList = [];

        loadAllCommentsForCurrentCommit();

        function loadAllCommentsForCurrentCommit() {
            if(currentCommit.isSelected()) {
                Comments.query({id: currentCommit.id}, function(data) {
                    $scope.commentsList = data.comments;
                })
            }
        }

        $scope.addComment = {
            commitId: currentCommit.id,
            body: ''
        };

        $scope.submitComment = function () {
            Comments.save($scope.addComment, function (comment) {
                $scope.commentsList.push(comment);
            })
        }

    })

