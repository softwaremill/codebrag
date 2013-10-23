angular.module('codebrag.commits')

    .controller('DiffNavbarCtrl', function ($scope, currentCommit, commitsService, $state) {

        $scope.markCurrentCommitAsReviewed = function () {
            var toRemove = $scope.currentCommit.info.id;
            currentCommit.empty();
            commitsService.markAsReviewed(toRemove).then(function(nextCommit) {
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
                return $state.transitionTo('commits.details', {id: nextCommit.id});
            }
            $state.transitionTo('commits.list');
        }

    });

