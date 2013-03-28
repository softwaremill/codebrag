angular.module('codebrag.commits')

    .factory('currentCommit', function() {
        var currentCommit = {
            id: undefined,
            sha: undefined
        };

        return currentCommit;
    });