angular.module('codebrag.commits')

    .controller('CommitDetailsCtrl', function ($scope, currentCommit, Files) {
        $scope.currentCommit = currentCommit;
        $scope.files = [];

        if (currentCommit.isSelected()) {
            Files.get({sha: currentCommit.sha}, function (files) {
                $scope.files = files;
            }, function (error) {
                console.error(error);
            });
        }

    });


