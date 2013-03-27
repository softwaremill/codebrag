angular.module('codebrag.commits')

    .controller('CommitDetailsCtrl', function($scope, currentCommit) {
        $scope.commitId = currentCommit.id;
    });


