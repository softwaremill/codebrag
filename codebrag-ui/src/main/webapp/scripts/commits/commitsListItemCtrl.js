angular.module('codebrag.commits')

    .controller('CommitsListItemCtrl', function($state, $stateParams, $scope, commitsListService) {

        $scope.openCommitDetails = function(commit) {
            $state.transitionTo('commits.details', {id: commit.id});
        };

        $scope.markAsReviewed = function(commit) {
			commitsListService.removeCommitAndGetNext(commit.id).then(function(nextCommit) {
                goTo(nextCommit);
            });
        };

        function goTo(nextCommit) {
            if (_.isNull(nextCommit)) {
                $state.transitionTo('commits.list');
            } else {
                $state.transitionTo('commits.details', {id: nextCommit.id});
            }
        }

    });