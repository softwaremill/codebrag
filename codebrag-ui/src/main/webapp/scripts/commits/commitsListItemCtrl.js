angular.module('codebrag.commits')

    .controller('CommitsListItemCtrl', function($state, $scope) {

        $scope.openCommitDetails = function(commit) {
            $state.transitionTo('commits.details', {id: commit.id});
        }

    });