angular.module('codebrag.commits')

    .factory('currentCommit', function () {
        var currentCommit;
        return {
            set: function (newCommit) {
                currentCommit = newCommit;
            },
            get: function () {
                return currentCommit;
            },
            empty: function() {
                currentCommit = null;
            }
        };
    })

    .controller('CommitDetailsCtrl', function ($stateParams, $state, $scope, commitsService, currentCommit) {

        var sha = $stateParams.sha;

        commitsService.commitDetails(sha).then(function (commit) {
            var current = new codebrag.CurrentCommit(commit);
            $scope.currentCommit = current;
            currentCommit.set(current);
        });

        function currentCommitPresent() {
            return currentCommit.get();
        }

        $scope.$watch(currentCommitPresent, function(present) {
            !present && delete $scope.currentCommit;
        })

    });


