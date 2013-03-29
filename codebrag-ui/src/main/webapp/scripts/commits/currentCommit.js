angular.module('codebrag.commits')

    .factory('currentCommit', function() {
        var currentCommit = {
            id: undefined,
            sha: undefined,
            isSelected: function() {
                return this.id !== undefined
            }
        };

        return currentCommit;
    });