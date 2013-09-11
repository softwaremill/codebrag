angular.module('codebrag.commits.comments')
    .factory('Comments', function ($resource) {
        return $resource('rest/commits/:id/comments', {id: '@commitId'}, {
            'query': {method: 'GET', isArray: false},
            'save': {method: 'POST', unique: true, requestId: 'postReaction'}
        });
    })

    .factory('Likes', function ($resource) {
        return $resource('rest/commits/:commitId/likes/:likeId', {commitId: '@commitId', likeId: '@likeId'}, {
            'query': {method: 'GET', isArray: false},
            'save': {method: 'POST', unique: true, requestId: 'postReaction'},
            'delete': {method: 'DELETE', unique: true, requestId: 'unlike'}
        });
    });