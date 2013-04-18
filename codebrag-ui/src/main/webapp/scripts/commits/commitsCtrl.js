angular.module('codebrag.commits')

    .controller('CommitsCtrl', function ($scope, $http, commitsListService) {

        commitsListService.loadCommitsFromServer();

        $scope.commits = commitsListService.allCommits;

        $scope.syncCommits = function() {
            $http({method: 'POST', url: 'rest/commits/sync'})
                .success(function(data) {
                    $scope.commits = data.commits;
                })
        };

    });