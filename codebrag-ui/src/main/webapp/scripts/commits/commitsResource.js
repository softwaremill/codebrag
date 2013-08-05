angular.module('codebrag.commits')
    .factory('Commits', function ($resource) {
        return $resource('rest/commits/:id', {}, {
            'query': {method: 'GET', isArray: false, requestType: 'commitsList'},
            'get': {method: 'GET', isArray: false}
        });
    });