angular.module('codebrag.commits')

    .controller('DiffNavbarCtrl', function ($scope, currentCommit, commitsService, $state) {

        $scope.markCurrentCommitAsReviewed = function () {
            var nextCommit = commitsService.markAsReviewed($scope.currentCommit.info.id);
            goTo(nextCommit);
        };

        var commitAvailable = function() {
            return currentCommit.get();
        };

        $scope.$watch(commitAvailable, function(commit) {
            $scope.currentCommit = commit;
        });

        function goTo(nextCommit) {
            currentCommit.empty();
            if (nextCommit) {
                return $state.transitionTo('commits.details', {id: nextCommit.id});
            }
            $state.transitionTo('commits.list');
        }

    });

