angular.module('codebrag.commits')

    .controller('DiffNavbarCtrl', function ($scope, currentCommit, commitsService, $state) {

        $scope.markCurrentCommitAsReviewed = function () {
            var shaToRemove = $scope.currentCommit.info.sha;
            currentCommit.empty();
            commitsService.markAsReviewed(shaToRemove).then(function(nextCommit) {
                goTo(nextCommit);
            })
        };

        var commitAvailable = function() {
            return currentCommit.get();
        };

        $scope.$watch(commitAvailable, function(commit) {
            $scope.currentCommit = commit;
        });

        function goTo(nextCommit) {
            if (nextCommit) {
                return $state.transitionTo('commits.details', {sha: nextCommit.sha});
            }
            $state.transitionTo('commits.list');
        }

    });

