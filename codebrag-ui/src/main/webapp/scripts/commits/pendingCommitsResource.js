angular.module('codebrag.commits')

    .factory('PendingCommits', function($resource) {
        var pendingCommitQueryParams = {type: 'pending'};
        return $resource('rest/commits', pendingCommitQueryParams);
    });

