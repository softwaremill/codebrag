angular.module('codebrag.commits')

    .factory('Files', function ($resource) {
        return $resource('rest/commits/:id', {id: "@id"}, {'get': {method: 'GET', isArray: true}});
    });

