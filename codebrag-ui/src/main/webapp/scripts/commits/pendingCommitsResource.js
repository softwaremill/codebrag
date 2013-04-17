angular.module('codebrag.commits')

    .factory('PendingCommits', function($resource) {
        return $resource('rest/commits/:id', {id: "@id"});
    });

