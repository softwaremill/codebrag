angular.module('codebrag.commits')

    .controller('CommitsCtrl', function CommitsCtrl($location, $scope, $http, PendingCommits) {
        PendingCommits.get(function(responseData) {
            $scope.commits = responseData.commits;
        });

        $scope.formatCommitDate = function(date) {
            var parsed = moment(date);
            return parsed.fromNow();
        }

        $scope.syncCommits = function() {
            $http({method: 'POST', url: 'rest/commits/sync'})
                .success(function(data) {
                    $scope.commits = data.commits;
                })
                .error(function() {
                    $location.path("/error500");
                });
        }
    })

    .factory('PendingCommits', function($resource) {
        var pendingCommitQueryParams = {type: 'pending'};
        return $resource('rest/commits', pendingCommitQueryParams);
    });

