angular.module('codebrag.commits')

    .controller('DiffNavbarCtrl', function ($scope, currentCommit, commitsListService, $state) {

        $scope.markCurrentCommitAsReviewed = function () {
            commitsListService.makeReviewedAndGetNext($scope.currentCommit.info.id).then(function (nextCommit) {
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
            if (_.isNull(nextCommit)) {
                $state.transitionTo('commits.list');
            } else {
                $state.transitionTo('commits.details', {id: nextCommit.id});
            }
        }

    });

