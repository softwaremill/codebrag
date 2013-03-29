angular.module('codebrag.commits')

    .factory('Files', function ($resource) {
        return $resource('rest/commits/:sha', {sha: "@sha"}, {'get': {method: 'GET', isArray: true}});
    });

