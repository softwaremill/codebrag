angular.module('codebrag.commits')

    .controller('CommitsCtrl', function CommitsCtrl($scope, PendingCommits) {
        PendingCommits.get(function(responseData) {
            $scope.commits = responseData.commits;
        });
    })

    .factory('PendingCommits', function($resource) {
        var pendingCommitQueryParams = {type: 'pending'};
        return $resource('rest/commits', pendingCommitQueryParams);
    });

