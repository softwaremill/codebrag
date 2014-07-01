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
            },
            isEmpty: function() {
                return angular.isUndefined(currentCommit) || currentCommit === null;
            },
            hasSha: function(sha) {
                if(this.isEmpty()) return false;
                return currentCommit.info.sha === sha
            }
        };
    })

    .controller('CommitDetailsCtrl', function ($stateParams, $scope, commitsService, currentCommit) {

        commitsService.commitDetails($stateParams.sha, $stateParams.repo).then(function (commit) {
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

