angular.module('codebrag.commits')

    .controller('CommitsCtrl', function CommitsCtrl($location, $scope, $http, PendingCommits) {
        PendingCommits.get(function(responseData) {
            $scope.commits = responseData.commits;
        });

        $scope.syncCommits = function() {
            $http({method: 'POST', url: 'rest/commits/sync'})
                .success(function(data) {
                    $scope.commits = data.commits;
                })
                .error(function() {
                    $location.path("/error500");
                });
        }
    });


