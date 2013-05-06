angular.module('codebrag.commits')
    .factory('Commits', function ($resource) {
        return $resource('rest/commits/:id');
    });