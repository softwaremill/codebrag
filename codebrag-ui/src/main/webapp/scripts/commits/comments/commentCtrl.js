'use strict';

angular.module('codebrag.commits.comments')

    .controller('CommentCtrl',function ($scope, currentCommit, Comments) {

        $scope.addComment = {
            commitId: currentCommit.id,
            body: ''
        }

        $scope.submitComment = function () {
            Comments.save($scope.addComment, function () {
                // callback here
            })
        }

    })

