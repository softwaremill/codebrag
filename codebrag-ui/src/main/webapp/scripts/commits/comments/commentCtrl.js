angular.module('codebrag.commits.comments')

    .controller('CommentCtrl', function($scope) {

        $scope.commentContent = '';

        $scope.submitComment = function () {
            console.log('TODO: will submit comment', $scope.commentContent);
        }

    });