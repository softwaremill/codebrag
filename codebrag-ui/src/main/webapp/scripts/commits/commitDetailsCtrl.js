angular.module('codebrag.commits')

    .controller('CommitDetailsCtrl', function ($stateParams, $scope, Files) {

        $scope.files = [];

        $scope.commitId = $stateParams.id

        Files.query({id: $stateParams.id}, function (files) {
            $scope.files = files;
        }, function (error) {
            console.error(error);
        });

    });


