angular.module('codebrag.branches')

    .factory('UserBrowsingContext', function($resource) {

        var params = { repo: '@repo' },
            actions = { save: { method: 'PUT' } };

        return $resource('rest/browsing-context/:repo', params, actions);

    });