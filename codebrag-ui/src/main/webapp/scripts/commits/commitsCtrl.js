angular.module('codebrag.commits')

    .config(function($routeProvider) {
        $routeProvider.when("/commits", {controller: 'CommitsCtrl', templateUrl: "views/commits.html"});
    })


    .factory('PendingCommits', function($resource) {
        var pendingCommitQueryParams = {type: 'pending'};
        return $resource('rest/commits', pendingCommitQueryParams);
    })

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


