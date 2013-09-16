angular.module('codebrag.commits')

    .controller('DiffNavbarCtrl', function ($scope, currentCommit, commitsListService, $state) {

        $scope.markCurrentCommitAsReviewed = function () {
            commitsListService.makeReviewedAndGetNext($scope.currentCommit.info.id).then(function (nextCommit) {
                goTo(nextCommit);
            });
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
            currentCommit.empty();
            $state.transitionTo('commits.list');
        }

    });

