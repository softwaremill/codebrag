angular.module('codebrag.commits')

    .controller('CommitsListItemCtrl', function($state, $stateParams, $scope, commitsListService) {

        $scope.openCommitDetails = function(commit) {
            $state.transitionTo('commits.details', {id: commit.id});
        };

        $scope.markAsReviewed = function(commit) {
			commitsListService.removeCommit(commit.id).then(function() {
                _getOutOfCommitDetailsIfCurrentRemoved(commit.id);
            });
        };

        function _getOutOfCommitDetailsIfCurrentRemoved(commitId) {
            if (commitId === $stateParams.id) {
                $state.transitionTo('commits.list');
            }
        }

    });