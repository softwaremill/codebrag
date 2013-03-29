angular.module('codebrag.commits')

    .controller('CommitDetailsCtrl', function ($scope, currentCommit, Files) {
        $scope.currentCommit = currentCommit;
        $scope.files = [];

        if (typeof currentCommit.sha != "undefined" && $scope.files.length == 0) {
            Files.get({sha: currentCommit.sha}, function (files) {
                $scope.files = files;
            }, function (error) {
                console.error(error);
            });
        }

    });


