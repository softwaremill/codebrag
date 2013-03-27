angular.module('codebrag.commits')

    .factory('currentCommit', function() {
        var currentCommit = {
            id: undefined
        };

        return currentCommit;
    });