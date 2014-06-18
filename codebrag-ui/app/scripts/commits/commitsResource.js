angular.module('codebrag.commits')

    .factory('Commits', function ($resource) {

        var commitsListLoadingRequest = 'commitsList';

        var loadCommitsToReviewParams = {filter: 'to_review'};
        var loadAllCommits = {filter: 'all'};
        var loadAllCommitsWithSurroundings = angular.extend({}, loadAllCommits, {context: true});

        return $resource('rest/commits/:repo/:sha', {}, {
            queryReviewable: {method: 'GET', isArray: false, requestType: commitsListLoadingRequest, params: loadCommitsToReviewParams},
            queryAllWithSurroundings: {method: 'GET', isArray: false, requestType: commitsListLoadingRequest, params: loadAllCommitsWithSurroundings},
            queryAll: {method: 'GET', isArray: false, requestType: commitsListLoadingRequest, params: loadAllCommits},
            query: {method: 'GET', isArray: false, requestType: commitsListLoadingRequest},
            querySilent: {method: 'GET', isArray: false},
            get: {method: 'GET', isArray: false}
        });
    });