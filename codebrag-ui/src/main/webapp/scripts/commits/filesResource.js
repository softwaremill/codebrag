angular.module('codebrag.commits')

    .factory('Files', function ($resource) {
        return $resource('rest/commits/:id/files', {id: "@id"});
    });

