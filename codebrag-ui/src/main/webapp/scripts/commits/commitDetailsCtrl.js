angular.module('codebrag.commits')

    .factory('currentCommit', function () {
        var currentCommit;
        return {
            set: function (newCommit) {
                currentCommit = newCommit
            },
            get: function () {
                return currentCommit;
            },
            empty: function() {
                currentCommit = null;
            }
        }
    })

    .controller('CommitDetailsCtrl', function ($stateParams, $state, $scope, commitsListService, currentCommit) {

        var commitId = $stateParams.id;

        commitsListService.loadCommitDetails(commitId).then(function (commit) {
            var current = new codebrag.CurrentCommit(commit);
            $scope.currentCommit = current;
            currentCommit.set(current);
        });

    });


