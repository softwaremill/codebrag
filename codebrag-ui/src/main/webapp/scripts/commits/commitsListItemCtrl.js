angular.module('codebrag.commits')

    .controller('CommitsListItemCtrl', function($state, $scope, commitsListService) {

        $scope.openCommitDetails = function(commit) {
            $state.transitionTo('commits.details', {id: commit.id});
        };

        $scope.markAsReviewed = function(commit, $event) {
			commitsListService.removeCommit(commit.id);
        }

    });